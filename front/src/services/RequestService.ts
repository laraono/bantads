import type { Request, RequestStatus } from '@/models/Request';
import { requests } from './seed';

const LS_KEY = 'requests'

export class RequestService {

    listAll(): Request[] {
        const requestsList = localStorage[LS_KEY];

        if (!requestsList) {
            localStorage.setItem(LS_KEY, JSON.stringify(requests))
            return [...requests]
        }
      
        return requestsList ? JSON.parse(requestsList) : requests;
    }

    insert(request: Request): void {
        const requests = this.listAll();
        request.id = new Date().getTime();
        requests.push(request);
        localStorage[LS_KEY] = JSON.stringify(requests);
    }

    findById(id: number): Request | undefined {
        const requests = this.listAll();
        return requests.find(request => request.id === id);
    }

    findByStatus(status: RequestStatus): Request[] {
        const requests = this.listAll();
        return requests.filter(request => request.status === status);
    }

    update(request: Request): void {
        const requests = this.listAll();
        requests.forEach( (obj, index, objs) => {
            if (request.id === obj.id) {
            objs[index] = request
        }
        });
        localStorage[LS_KEY] = JSON.stringify(requests);
    }

}