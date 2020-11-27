package com.example.restwebservice;

import com.google.common.annotations.VisibleForTesting;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Response;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@RestController
public class ContactController {

    @Autowired
    Client client;

    /**
     * Read: Get a list of contacts
     */
    @ApiOperation(value = "Get a list of contacts", response = ContactsResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get list of contacts"),
            @ApiResponse(code = 500, message = "Failed to process the request")
    })
    @GetMapping("/contacts")
    public ResponseEntity<ContactsResponse> getContacts(
            @RequestParam(value = "firstName", defaultValue = "") String firstName,
            @RequestParam(value = "lastName", defaultValue = "") String lastName,
            @RequestParam(value = "country", defaultValue = "") String country,
            @RequestParam(value = "phone", defaultValue = "") String phone,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "size", defaultValue = "20") Integer size
    ) {
        try {
            SearchResponse response = client.prepareSearch(Config.ES_INDEX)
                    .setTypes(Config.ES_CONTACT)
                    .setSearchType(SearchType.QUERY_THEN_FETCH)
                    .setQuery(buildQuery(firstName, lastName, country, phone))
                    .setFrom(from).setSize(size)
                    .addSort("lastModified", SortOrder.DESC)
                    .get();

            if (response.status().getStatus() == 200) {
                final List<SearchHit> searchHits = Arrays.asList(response.getHits().getHits());
                final List<Contact> contacts = searchHits.stream().map(hit -> {
                    Map<String, Object> map = hit.getSourceAsMap();
                    return new Contact(hit.getId(),
                            (String) map.get("firstName"), (String) map.get("lastName"),
                            (String) map.get("country"), (String) map.get("phone"), (Long) map.get("lastModified"));
                }).collect(Collectors.toList());

                return ResponseEntity.ok()
                        .body(new ContactsResponse(response.getHits().getTotalHits(), contacts));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ContactsResponse("Cannot get contacts"));
            }

        } catch (IndexNotFoundException e) {
            // not index yet, so return empty collection result
            return ResponseEntity.ok().body(new ContactsResponse());
        }
    }


    /**
     * Create: create a contact
     */
    @ApiOperation(value = "Create a contact", response = ContactsResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully create a contact"),
            @ApiResponse(code = 500, message = "Failed to create a contact")
    })
    @PostMapping("/contacts")
    public ResponseEntity<ContactsResponse> createContact(@RequestBody Contact contact) {
        try {
            contact.setLastModified(Instant.now().toEpochMilli());
            IndexResponse response = client.prepareIndex(Config.ES_INDEX, Config.ES_CONTACT)
                    .setSource(createContent(contact))
                    .get();

            if (response.status().getStatus() == 201) {
                contact.setId(response.getId());
                return ResponseEntity.ok().body(new ContactsResponse(contact));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ContactsResponse("Failed to create contact"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ContactsResponse("Error: something went wrong"));
        }
    }


    /**
     * Read: Get a contact by ID
     */
    @ApiOperation(value = "Get a contact by ID", response = ContactsResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get a contact"),
            @ApiResponse(code = 404, message = "Contact not found")
    })
    @GetMapping("/contacts/{ID}")
    public ResponseEntity<ContactsResponse> getContactById(@PathVariable(value="ID") String id) {

        GetResponse getResponse = client.prepareGet(Config.ES_INDEX, Config.ES_CONTACT, id).get();

        if (getResponse.isExists()) {
            Map<String, Object> fieldMap = getResponse.getSourceAsMap();
            Contact contact = new Contact(id,
                    (String) fieldMap.get("firstName"), (String) fieldMap.get("lastName"),
                    (String) fieldMap.get("country"), (String) fieldMap.get("phone"),
                    (Long) fieldMap.get("lastModified")
            );
            return ResponseEntity.ok().body(new ContactsResponse(contact));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ContactsResponse("Error: contact with id=" + id + " not found"));
        }
    }


    /**
     * Update: Update a contact by ID
     */
    @ApiOperation(value = "Update a contact by ID", response = ContactsResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully update the contact"),
            @ApiResponse(code = 404, message = "Contact not found for update"),
            @ApiResponse(code = 500, message = "Failed to update the contact")
    })
    @PutMapping("/contacts/{ID}")
    public ResponseEntity<ContactsResponse> updateContactById(@PathVariable(value="ID") String id,
                                                              @RequestBody Contact contact) {
        try {
            contact.setLastModified(Instant.now().toEpochMilli());
            UpdateResponse updateResponse = client.prepareUpdate(Config.ES_INDEX, Config.ES_CONTACT, id)
                    .setDoc(createContent(contact))
                    .get();
            if (updateResponse.status().getStatus() == 200) {
                contact.setId(id);
                return ResponseEntity.ok().body(new ContactsResponse(contact));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ContactsResponse("Error: update not successful"));
            }
        } catch (DocumentMissingException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ContactsResponse("Error: contact with id=" + id + " not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ContactsResponse("Error: something went wrong"));
        }
    }


    /**
     * Delete: Delete a contact by ID
     */
    @ApiOperation(value = "Delete a contact by ID", response = ContactsResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete the contact"),
            @ApiResponse(code = 404, message = "Contact not found for delete")
    })
    @DeleteMapping("/contacts/{ID}")
    public ResponseEntity<ContactsResponse> deleteContactById(@PathVariable(value="ID") String id) {
        DeleteResponse deleteResponse = client.prepareDelete(Config.ES_INDEX, Config.ES_CONTACT, id).get();
        if (deleteResponse.status().getStatus() == 200) {
            return ResponseEntity.ok()
                    .body(new ContactsResponse("Successfully delete record with id=" + id));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ContactsResponse("Error: contact with id=" + id + " not found" + id));
        }
    }


    /**
     * Create a QueryBuilder given the possible query string of each field in Contact instance
     * All search term should be concatenated with AND operator
     * All search term is perform CONTAIN search
     *
     * @param firstName search term for firstName
     * @param lastName  search term for lastName
     * @param country   search term for country
     * @param phone     search term for phone number
     * @return
     */
    @VisibleForTesting
    QueryBuilder buildQuery(String firstName, String lastName, String country, String phone) {
        String query = "";
        if (!firstName.isEmpty()) {
            query += "firstName:" + "*" + firstName + "*";
        }
        if (!lastName.isEmpty()) {
            if (!query.isEmpty()) query += " AND ";
            query += "lastName:" + "*" + lastName + "*";
        }
        if (!country.isEmpty()) {
            if (!query.isEmpty()) query += " AND ";
            query += "country:" + "*" + country + "*";
        }
        if (!phone.isEmpty()) {
            if (!query.isEmpty()) query += " AND ";
            query += "phone:" + "*" + phone + "*";
        }
        if (query.isEmpty()) {
            return QueryBuilders.matchAllQuery();
        } else {
            return QueryBuilders.queryStringQuery(query);
        }
    }


    /**
     * Create a XContentBuilder from a Contact instance
     *
     * @param contact
     * @return
     * @throws IOException
     */
    private XContentBuilder createContent(Contact contact) throws IOException {
        return jsonBuilder()
                .startObject()
                .field("firstName", contact.getFirstName())
                .field("lastName", contact.getLastName())
                .field("country", contact.getCountry())
                .field("phone", contact.getPhone())
                .field("lastModified", contact.getLastModified())
                .endObject();
    }
}
