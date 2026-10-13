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

        await redis.set(jobId, job)

        this.makeReport(jobId)

        return jobId
    }

    async makeReport(jobId) {
        try {
            const report = []

            const clients = await clientService.listClients()

            const accounts = []

            const managersCPF = []

            const headers = {'x-user-cpf': cpf}

            const managers = []

            clients.forEach(async client => {
                const account = await accountService.getAccountByCpf(client.cpf, headers) 
                accounts.push(account)

                if(!managersCPF.includes(account.cpfGerente)) {
                    managersCPF.push(account.cpfGerente)
                    const manager = await managerService.getManager(cpf)
                    managers.push(manager)
                }

                const managerName = managers.find(manager => manager.managerCPF === account.cpfGerente)

                const data = {
                    cpf: client.cpf, 
                    nome: client.nome, 
                    email: client.email, 
                    salario: client.salario, 
                    numeroConta: account.numeroConta,
                    saldo: account.saldo, 
                    cpfGerente: account.cpfGerente, 
                    nomeGerente: managerName.name
                }

                report.push(data)
            })

            const job = {
                jobId,
                status: "SUCESSO",
                resultType: "inline",
                dominio: null,
                resourceId: null,
                erro: null,
                resultado: report
            }

            redis.set(jobId, job)

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
