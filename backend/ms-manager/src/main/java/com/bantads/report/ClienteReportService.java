package com.bantads.report;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
            List<Map<String, Object>> clientes = montarRelatorio();
            jobService.complete(jobId, Map.of("clientes", clientes));
        } catch (Exception e) {
            jobService.fail(jobId, e.getMessage());
        }
    }

    private List<Map<String, Object>> montarRelatorio() {
        Map<String, Manager> gerentesPorCpf = new HashMap<>();
        for (Manager gerente : managerRepository.findAllByIsActive(true)) {
            gerentesPorCpf.put(gerente.getCpf(), gerente);
        }

        Map<String, Map<String, Object>> contasPorCpfCliente = new HashMap<>();
        for (String cpfGerente : gerentesPorCpf.keySet()) {
            for (Map<String, Object> conta : buscarContasDoGerente(cpfGerente)) {
                contasPorCpfCliente.put((String) conta.get("clientCPF"), conta);
            }
        }

        List<Map<String, Object>> linhas = new ArrayList<>();
        for (Map<String, Object> cliente : buscarClientes()) {
            String cpfCliente = (String) cliente.get("cpf");
            Map<String, Object> conta = contasPorCpfCliente.get(cpfCliente);
            if (conta == null) {
                continue; // cliente sem conta aberta ainda não entra no relatório
            }

            String cpfGerente = (String) conta.get("managerCPF");
            Manager gerente = gerentesPorCpf.get(cpfGerente);

            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("cpf", cpfCliente);
            linha.put("nome", cliente.get("name"));
            linha.put("email", cliente.get("email"));
            linha.put("salario", paraBigDecimal(cliente.get("salary")));
            linha.put("numeroConta", conta.get("accountNumber"));
            linha.put("saldo", paraBigDecimal(conta.get("balance")));
            linha.put("cpfGerente", cpfGerente);
            linha.put("nomeGerente", gerente != null ? gerente.getName() : null);
            linhas.add(linha);
        }

        linhas.sort(Comparator.comparing(linha -> chaveOrdenacao((String) linha.get("nome"))));
        return linhas;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buscarClientes() {
        Map<String, Object> resposta = restTemplate.getForObject(msClientBaseUrl + "/clients", Map.class);
        return extrairListaEmbutida(resposta);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buscarContasDoGerente(String cpfGerente) {
        List<Map<String, Object>> contas = restTemplate.getForObject(
                msAccountBaseUrl + "/accounts/managers/{cpf}/", List.class, cpfGerente);
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
