import Vue from 'vue'
import App from './App.vue'
import VueLogger from 'vuejs-logger';
import { BootstrapVue, BootstrapVueIcons} from 'bootstrap-vue'
import {BAlert} from "bootstrap-vue";
import Element from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'


Vue.use(BootstrapVue);
Vue.use(BootstrapVueIcons)
Vue.use(Element,{ locale })
Vue.config.productionTip = false;
Vue.component('b-alert', BAlert);

const options = {
  isEnabled: true,
  logLevel : 'debug',
  stringifyArguments : false,
  showLogLevel : true,
  showMethodName : false,
  separator: '|',
  showConsoleColors: true
};

Vue.use(VueLogger, options);

/* eslint-disable no-new */
new Vue({
  el: '#app',
  template: '<App/>',
  components: { App }
});