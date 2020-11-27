import axios from 'axios'

const SERVER_URL = 'http://localhost:8102';

const instance = axios.create({
    baseURL: SERVER_URL,
    timeout: 1000
});


export default {
    // Create
    createNew: (contact) => instance.post('contacts', contact, {}),
    // Read
    getAll: (query, from, pageSize) => {
        if (query != "") {
            return instance.get('contacts?'+ query +'&from=' + from + '&size=' + pageSize, {})
        } else {
            return instance.get('contacts?from=' + from + '&size=' + pageSize, {})
        }
    },
    // Update
    updateForId: (id, contact) => instance.put('contacts/'+id, contact, {}),
    // Delete
    removeForId: (id) => instance.delete('contacts/'+id)
}