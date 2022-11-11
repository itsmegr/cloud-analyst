package cloudsim.ext.datacenter;

import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 *
 * @author Manoj & Govind
 *
 */
public class LRUThrottling extends VmLoadBalancer implements CloudSimEventListener {

    private Map<Integer, VirtualMachineState> vmStatesList;

    //Add one list to keep track of last time when a VM was used
    private Map<Integer, Long> vmLastUsedTime = new HashMap<>();


  /**
     * @return VM id of the first available VM from the vmStatesList in the calling
     * 			{@link DatacenterController}
     */
    @Override
    public int getNextAvailableVm(){
        // get the vm which was last recently used among the available VMs
        int vmId = -1;
        long lastUsedTime = Long.MAX_VALUE;
        if(vmStatesList.size()>0){
            for (Iterator<Integer> it = vmStatesList.keySet().iterator(); it.hasNext();) {
                int vm = it.next();
                VirtualMachineState state = vmStatesList.get(vm);
                if (state.equals(VirtualMachineState.AVAILABLE)) {

                    if(vmLastUsedTime.get(vm)==null){
                        vmLastUsedTime.put(vm, System.currentTimeMillis());
                        allocatedVm(vm);
                        return vm;
                    }

                    if (vmLastUsedTime.get(vm) < lastUsedTime) {
                        lastUsedTime = vmLastUsedTime.get(vm);
                        vmId = vm;
                    }
                }
            }
        }
        

        // update the last used time of the vm to current system time
        vmLastUsedTime.put(vmId, System.currentTimeMillis());
        
        allocatedVm(vmId);
        return vmId;
    }


    public LRUThrottling(DatacenterController dcb){
        this.vmStatesList = dcb.getVmStatesList();
        dcb.addCloudSimEventListener(this);
    }


    public void cloudSimEventFired(CloudSimEvent e) {
        if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM){
            int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
            vmStatesList.put(vmId, VirtualMachineState.BUSY);
        } else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET){
            int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
            vmStatesList.put(vmId, VirtualMachineState.AVAILABLE);
        }
    }


}

