const jwt = require("jsonwebtoken");
const redis = require("../config/redis");

const JWT_SECRET = process.env.JWT_SECRET || "sua_chave_secreta";

function authGatewayFilter(requiredRole = null) {
  return async (req, res, next) => {
    const token = req.headers["x-access-token"];
    if (!token) {
      return res
        .status(401)
        .json({ auth: false, message: "Token não fornecido" });
    }

    try {
      const decoded = jwt.verify(token, JWT_SECRET);

      const jti = decoded.jti;

      const sessionKey = `sessao:${jti}`;
      const reverseSessionKey = `sessao:cpf:${decoded.cpf}`;

      const sessionExists = await redis.exists(sessionKey);

      if (!sessionExists) {
        return res
          .status(401)
          .json({ auth: false, message: "Falha ao autenticar o token" });
      }
      
      await redis.expire(sessionKey, 1800);
      await redis.expire(reverseSessionKey, 1800);

      if (requiredRole && decoded.tipo !== requiredRole) {
        return res
          .status(403)
          .json({ auth: false, message: "Acesso negado para este perfil" });
      }

      req.headers["x-user-cpf"] = decoded.cpf;
      req.headers["x-user-tipo"] = decoded.tipo;

      next();
    } catch (err) {
      console.log(err.message)
      return res
        .status(401)
        .json({ auth: false, message: "Falha ao autenticar o token" });
    }
  };
}

module.exports = { authGatewayFilter };
