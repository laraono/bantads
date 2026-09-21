import { reactive } from 'vue'

export const session = reactive({
  accountNumber: '',
  userCPF: '',
  userName: '',
  email: '',
})

export function setSession(data: { accountNumber: string; userCPF: string; userName: string; email: string }) {
  session.accountNumber = data.accountNumber
  session.userCPF = data.userCPF
  session.userName = data.userName
  session.email = data.email
}