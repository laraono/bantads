const axios = require('axios');

const MS_ACCOUNT_HOST = process.env.MS_ACCOUNT_HOST || 'ms-account';
const MS_ACCOUNT_PORT = process.env.MS_ACCOUNT_PORT || '8083';
const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || 'ms-client';
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || '8082';

class AccountService {
    async transfer(id, body) {
        const msBody = { contaDestino: body.contaDestino, valor: body.valor };
        const destinationCPF = await axios.get(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts/${msBody.contaDestino}/cpf`);
        const cpf = destinationCPF.data;

        if (!cpf ) {
            const error = new Error('Conta não existe');
            error.status = 422;
            throw error;
        }

        const originClient = await axios.get(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clients/${req.headers['x-user-cpf']}`)
        const destinationClient = await axios.get(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clients/${cpf}`)

        if(!originClient.data || !originClient.data.nome) {
            const error = new Error('Usuário não encontrado');
            error.status = 404;
            throw error;
        }

        if(!destinationClient.data || !destinationClient.data.nome) {
            const error = new Error('Usuário não existe');
            error.status = 422;
            throw error;
        }

        const transferAnswer = await axios.get(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts/${id}/transfer`, {
            body: {
                destino: {
                    conta: msBody.contaDestino,
                    nome: destinationClient.data.nome,
                    cpf
                },
                origem: {
                    nome: originClient.data.nome,
                    valor: msBody.valor
                }
            },
            headers: req.headers
        });

        if(!transferAnswer.data || !transferAnswer.data.destino) {
            const error = new Error('Transferência não finalizada');
            error.status = 500;
            throw error;
        }
        
        return transferAnswer.data.destino;
    }

}

module.exports = new AccountService();
