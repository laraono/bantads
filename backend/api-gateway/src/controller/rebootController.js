const rebootService = require('../service/rebootService');

class RebootController {
    async reboot(req, res) {
        try {
            const result = await rebootService.seedAuth();
            
            await rebootService.seedPostgres()

            await rebootService.flushRedis()

            return res.status(200).json({ 
                message: 'ok',
                status: 'ok',
                clientes: result.clientes,
                gerentes: result.gerentes,
                contas: result.contas,
            });
            
        } catch (err) {
            const status = err.status || 500;
            console.log(err)
            return res.status(status).json({ error: err.message || 'Falha no seeder' });
        }
    }
}

module.exports = new RebootController();