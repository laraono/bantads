const requestService = require('../service/requestService');

class RequestController {

    async listRequests(req, res) {
        try {
            const requests = await requestService.listRequests();

            return res.status(200).json(requests);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }

    async getRequest(req, res){
        try {
            const request = await requestService.getRequest(req.params.id);
            return res.status(200).json(request);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }

    async createRequest(req, res){
        try {
            const request = await requestService.createRequest(req.body);
            return res.status(201).json(request);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }


    async approveRequest(req, res){
        try {
            const request = await requestService.approveRequest(req.params.id);
            return res.status(202).json(request);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }

    async rejectRequest(req, res){
        try {
            const request = await requestService.rejectRequest(req.params.id, req.body);
            return res.status(200).json(request);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }
}

module.exports = new RequestController();