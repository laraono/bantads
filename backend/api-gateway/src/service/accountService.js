const axios = require('axios');

const MS_ACCOUNT_HOST = process.env.MS_ACCOUNT_HOST || 'ms-account';
const MS_ACCOUNT_PORT = process.env.MS_ACCOUNT_PORT || '8083';
const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || 'ms-client';
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || '8082';

class AccountService {
    async transfer(id, body, headers) {

        try {
            console.log('accoutn service')
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

            const originClient = await axios.get(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clients/${headers['x-user-cpf']}`, {
                headers: { 'x-user-cpf': headers['x-user-cpf']}
            })
            const destinationClient = await axios.get(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clients/${cpf}`, {
                headers: { 'x-user-cpf': headers['x-user-cpf']}
            })


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

            const transferAnswer = await axios.post(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/events/${id}/transfer`, {
                    payload: {destino: {
                        conta: msBody.contaDestino,
                        nome: destinationClient.data.nome,
                        cpf
                    },
                    origem: {
                        nome: originClient.data.nome,
                        valor: msBody.valor
                    }}
                },
                { headers: { 'x-user-cpf': headers['x-user-cpf']} }
            );

            if(!transferAnswer.data || !transferAnswer.data.destino) {
                const error = new Error('Transferência não finalizada');
                error.status = 500;
                throw error;
            }
            
            return transferAnswer.data.destino;
        } catch (err) {
            console.error('TRANSFER:', err.response?.data || err.message);
            throw err;  
        }
    }

    async withdraw(id, body, headers) {
        try {
            const withdrawAnswer = await axios.post(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/events/${id}/withdraw`,
                {payload: body},
                { headers: { 'x-user-cpf': headers['x-user-cpf']} }
            );

            return withdrawAnswer.data
        } catch (err) {
            console.error('WITHDRAW:', err.response?.data || err.message);
            throw err;  
        }
        
    }

    async deposit(id, body, headers) {
        try {
            const depositAnswwer = await axios.post(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/events/${id}/deposit`,
                {payload: body},
                { headers: { 'x-user-cpf': headers['x-user-cpf']} }
            );

            return depositAnswwer.data
        } catch (err) {
            console.error('DEPOSIT:', err.response?.data || err.message);
            throw err;  
        }
        
    }

    async getAccount(id, headers) {
        try {
            const accountAnswer = await axios.get(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts/${id}`, {
                headers: { 'x-user-cpf': headers['x-user-cpf']}
            });

            return accountAnswer.data
        } catch (err) {
            console.error('GET ACCOUNT:', err.response?.data || err.message);
            throw err;
        }
        
    }

    async getExtract(id, query, headers) {
        try {
            const extractAnswer = await axios.get(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts/${id}/extract`, {
                headers: { 'x-user-cpf': headers['x-user-cpf']},
                params: query
            });

            return extractAnswer.data
        } catch (err) {
            console.error('GET EXTRACT: ', err.response?.data || err.message);
            throw err;  
        }
        
    }

}

module.exports = new AccountService();
