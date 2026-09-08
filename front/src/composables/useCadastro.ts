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