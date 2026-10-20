const clientService = require('../service/clientService');

class ClientController {

    async listClients(req, res) {
        try {
            const clients = await clientService.listClients(req.query.busca);
            return res.status(200).json({ clients });
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