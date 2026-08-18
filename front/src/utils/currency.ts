import Decimal from 'decimal.js'

const BRL_FORMATTER = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
})

export function toDecimal(value: Decimal.Value): Decimal {
  return new Decimal(value)
}

export function formatCurrency(value: Decimal.Value): string {
  return BRL_FORMATTER.format(toDecimal(value).toNumber())
}

export function sum(values: Decimal.Value[]): Decimal {
  return values.reduce<Decimal>((total, value) => total.plus(value), new Decimal(0))
}
