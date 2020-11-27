import { shallowMount } from '@vue/test-utils';
import Table from '../../src/components/Table';

describe('<Table/>', () => {
    it('should render correct contents', () => {
        const wrapper = shallowMount(Table);
        expect(wrapper.find('div[id="app"]>div>h1').text()).toEqual('Contacts Table');
    });
});