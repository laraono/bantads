const clientService = require('../service/clientService');
const accountService = require('../service/accountService');

class ClientController {

    async listClients(req, res) {
        try {
            // API Composition (R11): dados cadastrais do MS Cliente + saldo do MS Conta (query)
            const [clients, accounts] = await Promise.all([
                clientService.listClients(req.query.busca),
                accountService.listAccounts(),
            ]);

            const contaPorCpf = new Map((accounts ?? []).map((a) => [a.clientCPF, a]));

            const clientes = clients
                .map((c) => {
                    const conta = contaPorCpf.get(c.cpf);
                    return {
                        cpf: c.cpf,
                        nome: c.nome,
                        email: c.email,
                        telefone: c.telefone,
                        salario: c.salario != null ? String(c.salario) : null,
                        cidade: c.cidade,
                        uf: c.uf,
                        numeroConta: conta?.accountNumber ?? null,
                        saldo: conta ? String(conta.balance) : null,
                    };
                })
                .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' }));

            return res.status(200).json({ clientes });
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }

    async getClient(req, res){
        try {
            const client = await clientService.getClient(req.params.cpf, req.headers);
            return res.status(200).json(client);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }
}

module.exports = new ClientController();