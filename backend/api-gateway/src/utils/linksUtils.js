const GATEWAY_URL = process.env.GATEWAY_URL || "http://localhost:3000";

// todos os caminhos possíveis, traduzidos para português
const DICIONARIO = {
  accounts: "contas",
  events: "contas",
  extract: "extrato",
  deposit: "deposito",
  withdraw: "saque",
  transfer: "transferencia",
  reject: "rejeicao",
  clients: "clientes",
  managers: "gerentes",
  requests: "solicitacoes",
  balance: null,
};

function translate(href, gatewayUrl) {
  const [base, template = ""] = href.split(/(?=\{)/);
  const segmentos = new URL(base).pathname.split("/").filter(Boolean);  // separa cada "/" do link
  const traduzidos = segmentos.map((s) => (s in DICIONARIO ? DICIONARIO[s] : s)); // e mapeia o que bater
  if (traduzidos.includes(null)) return null;
  return `${gatewayUrl}/${traduzidos.join("/")}${template}`;
}
function rewriteLinks(links, gatewayUrl = GATEWAY_URL) {
  const entries = Array.isArray(links) // se tivermos vários links
    ? links.map(({ rel, ...link }) => [rel, link])
    : Object.entries(links ?? {});

  const out = {};
  for (const [rel, { href, ...rest }] of entries) {
    const translated = translate(href, gatewayUrl);
    if (translated) out[rel] = { href: translated, ...rest };
  }
  return out;
}

module.exports = { rewriteLinks };
