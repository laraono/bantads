const axios = require('axios');

const MS_ACCOUNT_HOST = process.env.MS_ACCOUNT_HOST || 'ms-account';
const MS_ACCOUNT_PORT = process.env.MS_ACCOUNT_PORT || '8083';
const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || 'ms-client';
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || '8082';

class AccountService {
    async transfer(id, body, headers) {
        const msBody = { contaDestino: body.contaDestino, valor: body.valor };

        const destinationCPF = await axios.get(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts/${msBody.contaDestino}/cpf`, {
            headers: { 'x-user-cpf': headers['x-user-cpf']}
        });

        const cpf = destinationCPF.data;


        if (!cpf ) {
            const error = new Error('Conta não existe');
            error.status = 422;
            throw error;
        }

        const originClient = await axios.get(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clients/${headers['x-user-cpf']}/cpf`, {
            headers: { 'x-user-cpf': headers['x-user-cpf']}
        })

        if(!originClient.data) {
            const error = new Error('Usuário não encontrado');
            error.status = 404;
            throw error;
        }

        const destinationClient = await axios.get(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clients/${cpf}/cpf`, {
            headers: { 'x-user-cpf': headers['x-user-cpf']}
        })

        if(!destinationClient.data) {
            const error = new Error('Usuário não existe');
            error.status = 422;
            throw error;
        }

        const payload = {
            destino: {
                conta: msBody.contaDestino,
                nome: destinationClient.data,
                cpf
            },
            origem: {
                nome: originClient.data,
                valor: msBody.valor
            }
        }

        const transferAnswer = await axios.post(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/events/${id}/transfer`, {
                payload
            },
            { headers: { 'x-user-cpf': headers['x-user-cpf']} }
        );

        if(!transferAnswer.data) {
            const error = new Error('Transferência não finalizada');
            error.status = 500;
            throw error;
        }
        
        return transferAnswer.data;
    }

    async withdraw(id, body, headers) {
        const withdrawAnswer = await axios.post(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/events/${id}/withdraw`,
            {payload: body},
            { headers: { 'x-user-cpf': headers['x-user-cpf']} }
        )

        return withdrawAnswer.data
    }

    async deposit(id, body, headers) {
        const depositAnswwer = await axios.post(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/events/${id}/deposit`,
            {payload: body},
            { headers: { 'x-user-cpf': headers['x-user-cpf']} }
        )

        return depositAnswwer.data        
    }

    async getAccount(id, headers) {
        const accountAnswer = await axios.get(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts/${id}`, {
            headers: { 'x-user-cpf': headers['x-user-cpf']}
        })

        if(!accountAnswer.data) {
            const error = new Error('Conta não encontrado');
            error.status = 404;
            throw error;
        }

        if(!accountAnswer.data.numero || !accountAnswer.data.saldo || !accountAnswer.data.cpfCliente) {
            const error = new Error('Erro no servidor');
            error.status = 500;
            throw error;
        }

        return accountAnswer.data        
    }

    async getExtract(id, query, headers) {
        const extractAnswer = await axios.get(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts/${id}/extract`, {
            headers: { 'x-user-cpf': headers['x-user-cpf']},
            params: query
        });

        if(!extractAnswer.data) {
            const error = new Error('Conta não encontrado');
            error.status = 404;
            throw error;
        }

        if(!extractAnswer.data.saldoAbertura || !extractAnswer.data.movimentacoes) {
            const error = new Error('Erro no servidor');
            error.status = 500;
            throw error;
        }

        return extractAnswer.data
    }

}

module.exports = new AccountService();
