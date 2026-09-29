const accountController = require("./accountController");
const clientController = require("./clientController");

class ApiCompositionController {
  async listClients(req, res) {
    try {
      const { cpf, name } = req.query;

      const clientList = await clientController.listClients(cpf, name);

      if (!clientList || clientList.length === 0)
        return res.status(200).json({ clientes: [] });

      const accountsList = await accountController.listAccounts(
        clientList.map((c) => c.cpf),
      );

      const list = clientList.map((c) => {
        const account = accountsList.find(
          (a) => a.cpf === c.cpf,
        );
        
        return {
          cpf: c.cpf,
          nome: c.name,
          cidade: c.address.city,
          estado: c.address.state.name,
          saldo: account ? account.balance : 0,
        };
      });

      return res.status(200).json({ clientes: list });
    } catch (err) {
      const status = err.status || 500;
      return res
        .status(status)
        .json({ error: err.message || "Erro no servidor" });
    }
  }
}

module.exports = new ApiCompositionController();
