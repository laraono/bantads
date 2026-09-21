import { ref } from "vue";

const nome = ref('')
const cpf = ref('')
const telefone = ref('')
const email = ref('')
const cep = ref ('')
const numero = ref('')
const rua = ref('')
const complemento = ref('')
const cidade = ref('')
const estado = ref(null)
const salario = ref('')

export function resetCadastro() {
  nome.value = ''
  cpf.value = ''
  telefone.value = ''
  email.value = ''
  cep.value = ''
  numero.value = ''
  rua.value = ''
  complemento.value = ''
  cidade.value = ''
  estado.value = null
  salario.value = ''
}


export function useCadastro() {
    return {
        nome,
        cpf,
        telefone,
        email,
        cep,
        numero,
        rua,
        complemento,
        cidade,
        estado,
        salario
    }
}