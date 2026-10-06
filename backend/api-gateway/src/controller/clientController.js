const clientService = require('../service/clientService');

class ClientController {

    async listClients(req, res) {
        try {
            const clients = await clientService.listClients(req.query.busca);

            const clientes = clients
                .map((c) => ({
                    cpf: c.cpf,
                    nome: c.name,
                    email: c.email,
                    telefone: c.phone,
                    salario: c.salary,
                    cidade: c.address?.city,
                    uf: c.address?.state?.uf,
                }))
                .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' }));

            return res.status(200).json({ clientes });
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }

    async getClient(req, res){
        try {
            const client = await clientService.getClient(req.params.cpf);
            return res.status(200).json(client);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }
}

module.exports = new ClientController();