<template>
  <div>

    <h1>Contacts Table</h1>

    <!-- message notification -->
    <div id="message">
      <b-alert
              :show="successDismissCountDown"
              dismissible
              fade
              variant="success"
              @dismiss-count-down="successCountDownChanged"
      >
        {{ successMessage }}
      </b-alert>
      <b-alert
              :show="warnDismissCountDown"
              dismissible
              fade
              variant="warning"
              @dismiss-count-down="warnCountDownChanged"
      >
        {{ warnMessage }}
      </b-alert>
    </div>

    <!-- Top pagination control with create and search btm -->
    <div class="pagination">
      <button class="btn btn-secondary" v-on:click="toggleFilter"><b-icon icon="search"></b-icon></button>
      <button class="btn btn-success" v-on:click="toggleAdd"><b-icon icon="plus-circle"></b-icon></button>
      <el-pagination
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="currentPage"
              :page-sizes="[5, 10, 20, 50, 100]"
              :page-size="pagesize"
              layout="total, prev, pager, next, sizes"
              :total="totalContacts"
              prev-text="Previous"
              next-text="Next">
      </el-pagination>
    </div>

    <!-- Table of all available contacts, control of basic CRUD -->
    <table class="table table-hover table-bordered">
      <thead>
        <tr>
          <th>ID</th>
          <th>FirstName</th>
          <th>LastName</th>
          <th>Country</th>
          <th>Phone</th>
          <th>Action</th>
        </tr>
      </thead>

      <tbody>
        <tr v-show="showFilter">
          <!-- Search/Filter -->
          <td></td>
          <td><input type='text' v-model='firstNameFilter'></td>
          <td><input type='text' v-model='lastNameFilter'></td>
          <td><input type='text' v-model='countryFilter'></td>
          <td><input type='text' v-model='phoneFilter'></td>
          <td>
            <input type='button' class="btn btn-primary" value='Search' v-on:click='searchRecords()'>&nbsp;
            <input type='button' class="btn btn-secondary" value='Reset' v-on:click='resetQueryAndGetAllRecords()'></td>
        </tr>
        <tr v-show="showAdd">
          <!-- Add -->
          <td></td>
          <td><input type='text' v-model='firstName'></td>
          <td><input type='text' v-model='lastName'></td>
          <td><input type='text' v-model='country'></td>
          <td><input type='text' v-model='phone'></td>
          <td><input type='button' class="btn btn-success" value='Add' v-on:click='addRecord()'></td>
        </tr>

        <!-- Update/Delete -->
        <tr v-for='(contact,index) in contacts' :key="contact.id">
          <td><input type='text' v-model='contact.id' disabled></td>
          <td><input type='text' v-model='contact.firstName' ></td>
          <td><input type='text' v-model='contact.lastName' ></td>
          <td><input type='text' v-model='contact.country' ></td>
          <td><input type='text' v-model='contact.phone' ></td>
          <td>
            <input type='button' class="btn btn-primary" value='Update' v-on:click='updateRecord(index,contact.id)'>&nbsp;
            <input type='button' class="btn btn-danger" value='Delete' v-on:click='deleteRecord(index,contact.id)'>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Bottom pagination control -->
    <div class="pagination">
      <el-pagination
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="currentPage"
              :page-sizes="[5, 10, 20, 50, 100]"
              :page-size="pagesize"
              layout="total, prev, pager, next, sizes"
              :total="totalContacts"
              prev-text="Previous"
              next-text="Next">
      </el-pagination>
    </div>

  </div>
</template>

<script>
import api from '../Api';

export default {
  name: 'Table',
  data() {
    return {
      previousQuery: "",
      firstNameFilter: "",
      lastNameFilter: "",
      countryFilter: "",
      phoneFilter: "",
      showFilter: false,
      showAdd: false,
      currentPage: 1,
      pagesize: 5,
      successDismissSecs: 2,
      successDismissCountDown: 0,
      successMessage: "",
      warnDismissSecs: 2,
      warnDismissCountDown: 0,
      warnMessage: "",
      contacts: [],
      totalContacts: 0,
      firstName: "",
      lastName: "",
      country: "",
      phone: ""
    }
  },
  methods: {

    /**
     * Handle Read operations
     */
    // fetch all the record with appropriate search query
    allRecords: function(query){

      // calculate the from index from the current page and pagesize
      var fromIndex = (this.currentPage - 1) * this.pagesize;

      api.getAll(query, fromIndex, this.pagesize).then(res => {
        if (res.status == 200) {
          this.contacts = res.data.contacts;
          this.totalContacts = res.data.total;
          if (this.previousQuery != query) {
            this.showSuccessMessage("Filter successfully!");
            this.previousQuery = query;
          }
        } else {
          this.showWarnMessage("Failed to fetch contacts");
        }
      }).catch(err => {
        this.showWarnMessage("Error: Failed to fetch contact caused by " + err)
      })
    },
    // create a search query and fetch the corresponding records
    searchRecords: function() {
      var query = "";
      if (this.firstNameFilter != "") {
        query += "firstName=" + this.firstNameFilter;
      }
      if (this.lastNameFilter != "") {
        if (query != "") query += "&";
        query += "lastName=" + this.lastNameFilter;
      }
      if (this.countryFilter != "") {
        if (query != "") query += "&";
        query += "country=" + this.countryFilter;
      }
      if (this.phoneFilter != "") {
        if (query != "") query += "&";
        query += "phone=" + this.phoneFilter;
      }

      if (query != "") {
        // if new query exist, start the table from page 1
        if (this.previousQuery != query) {
          this.currentPage = 1;
        }
        this.allRecords(query);
      } else {
        this.showWarnMessage("Warning: at least one of search fields should not be empty")
      }
    },
    // reset the search query to empty, and fetch all records
    resetQueryAndGetAllRecords: function() {
      this.firstNameFilter = "";
      this.lastNameFilter = "";
      this.countryFilter = "";
      this.phoneFilter = "";
      this.allRecords("");
    },

    /**
     * Handle Create operations
     */
    addRecord: function(){
      if(this.firstName != "" || this.lastName != "" || this.country != "" || this.phone != ""){
        api.createNew({
          "firstName": this.firstName,
          "lastName": this.lastName,
          "country": this.country,
          "phone": this.phone}
        ).then(res => {

          // Empty values
          this.firstName = "";
          this.lastName = "";
          this.country = "";
          this.phone = "";

          if (res.status == 200) {
            this.showSuccessMessage("Success: Contact created!");
            // Fetch records
            setTimeout(()=> this.allRecords(""), 1000);
          } else {
            this.showWarnMessage("Failed to create contact");
          }

        }).catch(err => {
          this.showWarnMessage("Error: Failed to add contact caused by " + err)
        });

      }else{
        this.showWarnMessage("Warning: at least one of fields should not be empty")
      }

    },

    /**
     * Handle Update operations
     */
    updateRecord: function(index,id){

      // Read value from Textbox
      var firstName = this.contacts[index].firstName;
      var lastName = this.contacts[index].lastName;
      var country = this.contacts[index].country;
      var phone = this.contacts[index].phone;

      api.updateForId(id,{
        "firstName": firstName,
        "lastName": lastName,
        "country": country,
        "phone": phone}
      ).then(res => {

        if (res.status == 200) {
          this.showSuccessMessage("Success: Contact updated!");
          // Fetch records
          setTimeout(()=> this.allRecords(""), 1000);
        } else {
          this.showWarnMessage("Failed to update contact");
        }

      }).catch(err => {
        this.showWarnMessage("Error: Failed to update contact caused by " + err)
      });
    },

    /**
     * Handle Delete operations
     */
    deleteRecord: function(index,id){
      api.removeForId(id).then(res => {
        if (res.status == 200) {
          this.showSuccessMessage("Success: Contact deleted!");
          // Fetch records
          setTimeout(()=> this.allRecords(""), 1000);
        } else {
          this.showWarnMessage("Failed to delete contact");
        }
      }).catch(err => {
        this.showWarnMessage("Error: Failed to delete contact caused by " + err)
      });
    },

    /**
     * handle the page changes
     */
    handleSizeChange: function(size) {
      this.pagesize = size;
      this.allRecords("");
    },
    handleCurrentChange: function(currentPage) {
      this.currentPage = currentPage;
      this.allRecords("");
    },

    /**
     * Handle the top add and search btm, and input display
     */
    toggleFilter: function() {
      this.showFilter = !this.showFilter;
      if (this.showFilter && this.showAdd) {
        this.showAdd = false;
      }
    },
    toggleAdd: function() {
      this.showAdd = !this.showAdd;
      if (this.showFilter && this.showAdd) {
        this.showFilter = false;
      }
    },

    /**
     * Handle the message notification count down
     */
    successCountDownChanged(dismissCountDown) {
      this.successDismissCountDown = dismissCountDown
    },
    warnCountDownChanged(dismissCountDown) {
      this.warnDismissCountDown = dismissCountDown
    },
    showSuccessMessage(message) {
      this.successMessage = message;
      this.successDismissCountDown = this.successDismissSecs
    },
    showWarnMessage(message) {
      this.warnMessage = message;
      this.warnDismissCountDown = this.warnDismissSecs
    }
  },
  mounted() {
    this.allRecords("");
  }
}
</script>

<style scoped>
  #message {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
  }
</style>
