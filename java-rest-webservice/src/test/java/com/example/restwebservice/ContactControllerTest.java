package com.example.restwebservice;

import com.google.common.collect.ImmutableMap;
import org.elasticsearch.action.delete.DeleteAction;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetAction;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateAction;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContactControllerTest {

    private static final String ID = "id1";

    private ContactController contactController;
    private SearchRequestBuilder searchRequestBuilder;
    private IndexRequestBuilder indexRequestBuilder;
    private GetRequestBuilder getRequestBuilder;
    private UpdateRequestBuilder updateRequestBuilder;
    private DeleteRequestBuilder deleteRequestBuilder;



    @BeforeEach
    void setUp() throws Exception {
        this.contactController = new ContactController();
        this.contactController.client = Mockito.mock(TransportClient.class);

        this.searchRequestBuilder = spy(new SearchRequestBuilder(contactController.client, SearchAction.INSTANCE));
        this.searchRequestBuilder.setIndices(Config.ES_INDEX);
        when(this.contactController.client.prepareSearch(Config.ES_INDEX))
                .thenReturn(searchRequestBuilder);

        this.indexRequestBuilder = spy(new IndexRequestBuilder(contactController.client, IndexAction.INSTANCE, Config.ES_INDEX));
        this.indexRequestBuilder.setType(Config.ES_CONTACT);
        when(this.contactController.client.prepareIndex(Config.ES_INDEX, Config.ES_CONTACT))
                .thenReturn(this.indexRequestBuilder);

        this.getRequestBuilder = spy(new GetRequestBuilder(contactController.client, GetAction.INSTANCE, Config.ES_INDEX));
        this.getRequestBuilder.setType(Config.ES_CONTACT);
        when(this.contactController.client.prepareGet(Config.ES_INDEX, Config.ES_CONTACT, ID))
                .thenReturn(this.getRequestBuilder);

        this.updateRequestBuilder = spy(new UpdateRequestBuilder(contactController.client, UpdateAction.INSTANCE));
        when(this.contactController.client.prepareUpdate(Config.ES_INDEX, Config.ES_CONTACT, ID))
                .thenReturn(this.updateRequestBuilder);

        this.deleteRequestBuilder = spy(new DeleteRequestBuilder(contactController.client, DeleteAction.INSTANCE));
        when(this.contactController.client.prepareDelete(Config.ES_INDEX, Config.ES_CONTACT, ID))
                .thenReturn(this.deleteRequestBuilder);


    }

    @Test
    void testGetContactsRequestWithDefaultParameters() {
        // setup response
        SearchHits searchHits = SearchHits.empty();
        SearchResponse response = mock(SearchResponse.class);
        when(response.status()).thenReturn(RestStatus.OK);
        when(response.getHits()).thenReturn(searchHits);
        Mockito.doReturn(response).when(this.searchRequestBuilder).get();
        this.contactController.getContacts("", "", "", "", 0, 20);

        assertEquals("{\n" +
                "  \"from\" : 0,\n" +
                "  \"size\" : 20,\n" +
                "  \"query\" : {\n" +
                "    \"match_all\" : {\n" +
                "      \"boost\" : 1.0\n" +
                "    }\n" +
                "  },\n" +
                "  \"sort\" : [\n" +
                "    {\n" +
                "      \"lastModified\" : {\n" +
                "        \"order\" : \"desc\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}", this.searchRequestBuilder.toString());
    }

    @Test
    void testGetContactsRequestSomeParameters() {
        // setup response
        SearchHits searchHits = SearchHits.empty();
        SearchResponse response = mock(SearchResponse.class);
        when(response.status()).thenReturn(RestStatus.OK);
        when(response.getHits()).thenReturn(searchHits);
        Mockito.doReturn(response).when(searchRequestBuilder).get();
        final ResponseEntity responseEntity = this.contactController.getContacts(
                        "Anakin", "Ting", "CA", "519", 10, 100
        );


        assertEquals("{\n" +
                "  \"from\" : 10,\n" +
                "  \"size\" : 100,\n" +
                "  \"query\" : {\n" +
                "    \"query_string\" : {\n" +
                "      \"query\" : \"firstName:*Anakin* AND lastName:*Ting* AND country:*CA* AND phone:*519*\",\n" +
                "      \"fields\" : [ ],\n" +
                "      \"use_dis_max\" : true,\n" +
                "      \"tie_breaker\" : 0.0,\n" +
                "      \"default_operator\" : \"or\",\n" +
                "      \"auto_generate_phrase_queries\" : false,\n" +
                "      \"max_determinized_states\" : 10000,\n" +
                "      \"enable_position_increments\" : true,\n" +
                "      \"fuzziness\" : \"AUTO\",\n" +
                "      \"fuzzy_prefix_length\" : 0,\n" +
                "      \"fuzzy_max_expansions\" : 50,\n" +
                "      \"phrase_slop\" : 0,\n" +
                "      \"escape\" : false,\n" +
                "      \"split_on_whitespace\" : true,\n" +
                "      \"boost\" : 1.0\n" +
                "    }\n" +
                "  },\n" +
                "  \"sort\" : [\n" +
                "    {\n" +
                "      \"lastModified\" : {\n" +
                "        \"order\" : \"desc\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}", searchRequestBuilder.toString());
    }

    @Test
    void testGetContactsResponseWithHits() {
        // setup all hits
        SearchHit hit1 = mock(SearchHit.class);
        when(hit1.getId()).thenReturn("id1");

        when(hit1.getSourceAsMap()).thenReturn(ImmutableMap.of(
                "firstName", "anakin",
                "lastName", "Ting",
                "country", "CA",
                "phone", "519"
        ));
        SearchHit hit2 = mock(SearchHit.class);
        when(hit1.getId()).thenReturn("id2");
        when(hit1.getSourceAsMap()).thenReturn(ImmutableMap.of(
                "firstName", "Leo",
                "lastName", "Yang",
                "country", "US",
                "phone", "416"
        ));

        // setup response
        SearchHits searchHits = new SearchHits(new SearchHit[]{hit1, hit2}, 2, 0.1f);
        SearchResponse response = mock(SearchResponse.class);
        when(response.status()).thenReturn(RestStatus.OK);
        when(response.getHits()).thenReturn(searchHits);
        Mockito.doReturn(response).when(searchRequestBuilder).get();
        final ResponseEntity<ContactsResponse> responseEntity =
                this.contactController.getContacts("", "", "", "", 0, 20);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(2, responseEntity.getBody().getContacts().size());

    }

    @Test
    void testGetContactsResponseFailToRetrieve() {
        // setup response
        SearchResponse response = mock(SearchResponse.class);
        Mockito.doReturn(response).when(searchRequestBuilder).get();
        when(response.status()).thenReturn(RestStatus.INTERNAL_SERVER_ERROR);
        final ResponseEntity responseEntity =
                this.contactController.getContacts("", "", "", "", 0, 20);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void testGetContactsResponseWhenNoIndex() {
        // setup response
        Mockito.doThrow(new IndexNotFoundException("")).when(searchRequestBuilder).get();
        final ResponseEntity<ContactsResponse> responseEntity =
                this.contactController.getContacts("", "", "", "", 0, 20);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(0, responseEntity.getBody().getContacts().size());

    }

    @Test
    void testCreateContactSuccess() {
        // setup response
        IndexResponse response = mock(IndexResponse.class);
        when(response.status()).thenReturn(RestStatus.CREATED);
        when(response.getId()).thenReturn(ID);

        Mockito.doReturn(response).when(this.indexRequestBuilder).get();
        final ResponseEntity<ContactsResponse> responseEntity =
                this.contactController.createContact(
                        new Contact(null, "f", "l", "c", "p", null));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(1, responseEntity.getBody().getContacts().size());
        assertEquals(ID, responseEntity.getBody().getContacts().get(0).getId());
        assertNotNull(responseEntity.getBody().getContacts().get(0).getLastModified());
    }

    @Test
    void testCreateContactFail1() {
        // setup response
        IndexResponse response = mock(IndexResponse.class);
        when(response.status()).thenReturn(RestStatus.INTERNAL_SERVER_ERROR);

        Mockito.doThrow(new RuntimeException()).when(this.indexRequestBuilder).get();
        final ResponseEntity responseEntity =
                this.contactController.createContact(
                        new Contact(null, "f", "l", "c", "p", null));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void testCreateContactFail2() {
        Mockito.doThrow(new RuntimeException()).when(this.indexRequestBuilder).get();
        final ResponseEntity responseEntity =
                this.contactController.createContact(
                        new Contact(null, "f", "l", "c", "p", null));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void testGetContactByIdSuccess() {
        // setup response
        GetResponse response = mock(GetResponse.class);
        when(response.isExists()).thenReturn(true);
        when(response.getId()).thenReturn(ID);
        when(response.getSourceAsMap()).thenReturn(ImmutableMap.of(
                "firstName", "Leo",
                "lastName", "Yang",
                "country", "US",
                "phone", "416",
                "lastModified", 123123123L
        ));

        Mockito.doReturn(response).when(this.getRequestBuilder).get();
        final ResponseEntity<ContactsResponse> responseEntity = this.contactController.getContactById(ID);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Leo", responseEntity.getBody().getContacts().get(0).getFirstName());
        assertEquals(ID, responseEntity.getBody().getContacts().get(0).getId());
    }

    @Test
    void testGetContactByIdFail() {
        // setup response
        GetResponse response = mock(GetResponse.class);
        when(response.isExists()).thenReturn(false);

        Mockito.doReturn(response).when(this.getRequestBuilder).get();
        final ResponseEntity responseEntity = this.contactController.getContactById(ID);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void testUpdateContactByIdSuccess() {
        // setup response
        UpdateResponse response = mock(UpdateResponse.class);
        when(response.status()).thenReturn(RestStatus.OK);

        Mockito.doReturn(response).when(this.updateRequestBuilder).get();
        Contact contact = new Contact(null, "f", "l", "c", "p", null);
        final ResponseEntity<ContactsResponse> responseEntity = this.contactController.updateContactById(ID, contact);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(ID, responseEntity.getBody().getContacts().get(0).getId());
        assertNotNull(responseEntity.getBody().getContacts().get(0).getLastModified());
    }

    @Test
    void testUpdateContactByIdFail() {
        // setup response
        UpdateResponse response = mock(UpdateResponse.class);
        when(response.status()).thenReturn(RestStatus.INTERNAL_SERVER_ERROR);

        Mockito.doReturn(response).when(this.updateRequestBuilder).get();
        Contact contact = new Contact(null, "f", "l", "c", "p", null);
        final ResponseEntity responseEntity = this.contactController.updateContactById(ID, contact);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void testUpdateContactByIdDocumentMissing() {
        Mockito.doThrow(new DocumentMissingException(new ShardId("", "", 1), "", ""))
                .when(this.updateRequestBuilder).get();
        Contact contact = new Contact(null, "f", "l", "c", "p", null);
        final ResponseEntity responseEntity = this.contactController.updateContactById(ID, contact);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void testDeleteContactByIdSuccess() {
        // setup response
        DeleteResponse response = mock(DeleteResponse.class);
        when(response.status()).thenReturn(RestStatus.OK);

        Mockito.doReturn(response).when(this.deleteRequestBuilder).get();
        final ResponseEntity responseEntity = this.contactController.deleteContactById(ID);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testDeleteContactByIdFail() {
        // setup response
        DeleteResponse response = mock(DeleteResponse.class);
        when(response.status()).thenReturn(RestStatus.INTERNAL_SERVER_ERROR);

        Mockito.doReturn(response).when(this.deleteRequestBuilder).get();
        final ResponseEntity responseEntity = this.contactController.deleteContactById(ID);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }


    @Test
    void testBuildQuery() {
        QueryBuilder emptyQuery = contactController.buildQuery("", "", "", "");
        assertEquals("{\n" +
                "  \"match_all\" : {\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}", emptyQuery.toString());

        QueryBuilder qeuryOneSearchTerm = contactController.buildQuery("", "", "CA", "");
        assertEquals("{\n" +
                "  \"query_string\" : {\n" +
                "    \"query\" : \"country:*CA*\",\n" +
                "    \"fields\" : [ ],\n" +
                "    \"use_dis_max\" : true,\n" +
                "    \"tie_breaker\" : 0.0,\n" +
                "    \"default_operator\" : \"or\",\n" +
                "    \"auto_generate_phrase_queries\" : false,\n" +
                "    \"max_determinized_states\" : 10000,\n" +
                "    \"enable_position_increments\" : true,\n" +
                "    \"fuzziness\" : \"AUTO\",\n" +
                "    \"fuzzy_prefix_length\" : 0,\n" +
                "    \"fuzzy_max_expansions\" : 50,\n" +
                "    \"phrase_slop\" : 0,\n" +
                "    \"escape\" : false,\n" +
                "    \"split_on_whitespace\" : true,\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}", qeuryOneSearchTerm.toString());

        QueryBuilder qeuryThreeSearchTerm = contactController.buildQuery("An", "Ti", "", "123");
        assertEquals("{\n" +
                "  \"query_string\" : {\n" +
                "    \"query\" : \"firstName:*An* AND lastName:*Ti* AND phone:*123*\",\n" +
                "    \"fields\" : [ ],\n" +
                "    \"use_dis_max\" : true,\n" +
                "    \"tie_breaker\" : 0.0,\n" +
                "    \"default_operator\" : \"or\",\n" +
                "    \"auto_generate_phrase_queries\" : false,\n" +
                "    \"max_determinized_states\" : 10000,\n" +
                "    \"enable_position_increments\" : true,\n" +
                "    \"fuzziness\" : \"AUTO\",\n" +
                "    \"fuzzy_prefix_length\" : 0,\n" +
                "    \"fuzzy_max_expansions\" : 50,\n" +
                "    \"phrase_slop\" : 0,\n" +
                "    \"escape\" : false,\n" +
                "    \"split_on_whitespace\" : true,\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}", qeuryThreeSearchTerm.toString());
    }
}