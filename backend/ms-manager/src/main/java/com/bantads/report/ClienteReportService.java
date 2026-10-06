package com.bantads.report;

import com.bantads.dto.ClienteRelatorioDTO;
import com.bantads.entity.Manager;
import com.bantads.job.JobService;
import com.bantads.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ClienteReportService {

    private static final Pattern MARCAS_DIACRITICAS = Pattern.compile("\\p{M}");

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private JobService jobService;

    @Value("${services.ms-client.base-url}")
    private String msClientBaseUrl;

    @Value("${services.ms-account.base-url}")
    private String msAccountBaseUrl;

    @Async
    public void gerarRelatorioClientes(String jobId) {
        try {
            List<ClienteRelatorioDTO> clientes = montarRelatorio();
            jobService.complete(jobId, Map.of("clientes", clientes));
        } catch (Exception e) {
            jobService.fail(jobId, e.getMessage());
        }
    }

    private List<ClienteRelatorioDTO> montarRelatorio() {
        Map<String, Manager> gerentesPorCpf = managerRepository.findAll().stream()
                .collect(Collectors.toMap(Manager::getCpf, Function.identity()));

        Map<String, Map<String, Object>> contasPorCpfCliente = buscarTodasContas().stream()
                .collect(Collectors.toMap(conta -> (String) conta.get("clientCPF"), Function.identity()));

        // só entram no relatório clientes com conta aberta (cpf em contasPorCpfCliente)
        return buscarClientes().stream()
                .filter(cliente -> contasPorCpfCliente.containsKey((String) cliente.get("cpf")))
                .map(cliente -> montarLinha(cliente, contasPorCpfCliente.get((String) cliente.get("cpf")), gerentesPorCpf))
                .sorted(Comparator.comparing(linha -> chaveOrdenacao(linha.getNome())))
                .collect(Collectors.toList());
    }

    private ClienteRelatorioDTO montarLinha(Map<String, Object> cliente,
                                             Map<String, Object> conta,
                                             Map<String, Manager> gerentesPorCpf) {
        String cpfGerente = (String) conta.get("managerCPF");
        Manager gerente = gerentesPorCpf.get(cpfGerente);

        return ClienteRelatorioDTO.builder()
                .cpf((String) cliente.get("cpf"))
                .nome((String) cliente.get("name"))
                .email((String) cliente.get("email"))
                .salario(paraBigDecimal(cliente.get("salary")))
                .numeroConta((String) conta.get("accountNumber"))
                .saldo(paraBigDecimal(conta.get("balance")))
                .cpfGerente(cpfGerente)
                .nomeGerente(gerente != null ? gerente.getName() : null)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buscarClientes() {
        Map<String, Object> resposta = restTemplate.getForObject(msClientBaseUrl + "/clients", Map.class);
        return extrairListaEmbutida(resposta);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buscarTodasContas() {
        List<Map<String, Object>> contas = restTemplate.getForObject(msAccountBaseUrl + "/accounts", List.class);
        return contas != null ? contas : List.of();
    }

    // resposta HATEOAS: o nome da chave embutida varia conforme o tipo (ex.:
    // "clientList"), então pega o primeiro (e único) array em "_embedded" em
    // vez de fixar o nome da relação
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extrairListaEmbutida(Map<String, Object> resposta) {
        if (resposta == null) {
            return List.of();
        }
        Object embedded = resposta.get("_embedded");
        if (!(embedded instanceof Map<?, ?> embeddedMap) || embeddedMap.isEmpty()) {
            return List.of();
        }
        Object primeiraLista = embeddedMap.values().iterator().next();
        return primeiraLista instanceof List ? (List<Map<String, Object>>) primeiraLista : List.of();
    }

    private static BigDecimal paraBigDecimal(Object valor) {
        return valor == null ? null : new BigDecimal(valor.toString());
    }

    private static String chaveOrdenacao(String nome) {
        String normalizado = Normalizer.normalize(nome == null ? "" : nome, Normalizer.Form.NFKD);
        return MARCAS_DIACRITICAS.matcher(normalizado).replaceAll("").toLowerCase();
    }
}
