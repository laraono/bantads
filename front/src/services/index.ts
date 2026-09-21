// src/services/index.ts
import { AccountDataService } from './AccountDataService'
import { AccountHistoryService } from './AccountHistoryService'
import { AccountService } from './AccountService'
import { ClientService } from './ClientService'
import { EventService } from './EventService'
import { ManagerService } from './ManagerService'
import { RequestService } from './RequestService'

const accountDataService = new AccountDataService()
const accountHistoryService = new AccountHistoryService()
const accountService = new AccountService(accountDataService, accountHistoryService)
const eventService = new EventService(accountService)
const managerService = new ManagerService(eventService)
const clientService = new ClientService(managerService, eventService)
const requestService = new RequestService()

export { eventService, accountService, accountDataService, accountHistoryService, managerService, clientService, requestService }