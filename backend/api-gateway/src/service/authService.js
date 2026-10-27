const axios = require("axios");
const jwt = require("jsonwebtoken");
const crypto = require("crypto");
const redis = require("../config/redis");

const MS_AUTH_HOST = process.env.MS_AUTH_HOST || "ms-auth";
const MS_AUTH_PORT = process.env.MS_AUTH_PORT || "8081";
const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || "ms-client";
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || "8082";
const MS_MANAGER_HOST = process.env.MS_MANAGER_HOST || "ms-manager";
const MS_MANAGER_PORT = process.env.MS_MANAGER_PORT || "8084";
const JWT_SECRET = process.env.JWT_SECRET || "sua_chave_secreta";

class AuthService {
  unauthorizedError() {
    const error = new Error("Usuário não autorizado");
    error.status = 401;
    throw error;
  }

  async authenticate(body) {
    if (!body.email || !body.senha) {
      const error = new Error("Email e/ou senha não informados");
      error.status = 400;
      throw error;
    }

    const msBody = { email: body.email, senha: body.senha };
    let data;
    try {
      const authResponse = await axios.post(
        `http://${MS_AUTH_HOST}:${MS_AUTH_PORT}/auth/login`,
        msBody,
      );
      data = authResponse.data;
    } catch (err) {
      console.log(err.message)
      this.unauthorizedError();
    }

    if (!data?.auth) {
      this.unauthorizedError();
    }

    const jti = crypto.randomUUID();
    const payload = { jti, cpf: data.cpf, tipo: data.tipo };
    const token = jwt.sign(payload, JWT_SECRET, { expiresIn: "7d" });

    await redis.set(`sessao:${jti}`, "ativo", "EX", 1800);
    await redis.set(`sessao:cpf:${data.cpf}`, jti, "EX", 1800);

    const usuario = await this.composeUsuario(data.cpf, data.tipo);

    return { auth: true, token, tipo: data.tipo, usuario };
  }

  async composeUsuario(cpf, tipo) {
    try {
      if (tipo?.toUpperCase() === "GERENTE") {
        const { data } = await axios.get(
          `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers/${cpf}`,
        );
        return {
          cpf: data.cpf,
          nome: data.nome,
          email: data.email,
        };
      }
      const { data } = await axios.get(
        `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clientes/${cpf}`,
      );
      return { cpf: data.cpf, nome: data.nome, email: data.email };
    } catch (err) {
      console.log(err.message)
      return { cpf, nome: "", email: "" };
    }
  }

  async invalidateSession(tokenValue) {
    if (!tokenValue) {
      this.unauthorizedError();
    }

    const decoded = jwt.decode(tokenValue);

    if (decoded && decoded.jti) {
      await redis.del(`sessao:${decoded.jti}`);
      await redis.del(`sessao:cpf:${decoded.cpf}`);
    }
  }
}

module.exports = new AuthService();
