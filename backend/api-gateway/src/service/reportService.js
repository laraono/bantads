const redis = require('../config/redis');
const clientService = require('../service/clientService')
const accountService = require('../service/accountService')
const managerService = require('../service/managerService')

class JobService {

    async getReport() {
        const jobId = "job-" + crypto.randomUUID()

        const job = {
            jobId,
            status: "PENDENTE",
            resultType: "inline",
            dominio: null,
            resourceId: null,
            erro: null
        }

        await redis.set(jobId, JSON.stringify(job))

        this.makeReport(jobId)

        return {jobId}
    }

    async makeReport(jobId) {
        try {
            const report = []

            const clients = await clientService.listClients()

            const accounts = []

            const managers = {}

            for(const client of clients) {
                const headers = {'x-user-cpf': client.cpf}

                const account = await accountService.getAccountByCpf(client.cpf, headers) 
                accounts.push(account)

                if (account.cpfGerente && !managers[account.cpfGerente]) {
                    const manager = await managerService.getManager(account.cpfGerente)
                    managers[account.cpfGerente] = manager
                }

                const managerObj = managers[account.cpfGerente];

                const data = {
                    cpf: client.cpf, 
                    nome: client.nome, 
                    email: client.email, 
                    salario: client.salario, 
                    numeroConta: account.numero,
                    saldo: account.saldo, 
                    cpfGerente: account.cpfGerente, 
                    nomeGerente: managerObj.nome
                }

                report.push(data)
            }

            const job = {
                jobId,
                status: "CONCLUIDO",
                resultType: "inline",
                dominio: null,
                resourceId: null,
                erro: null,
                resultado: {clientes: report}
            }

            redis.set(jobId, JSON.stringify(job))

        } catch(err) {
            const job = {
                jobId,
                status: "FALHA",
                resultType: "inline",
                dominio: null,
                resourceId: null,
                erro: err.message
            }

            redis.set(jobId, job)
        }

    }

}

module.exports = new JobService();
