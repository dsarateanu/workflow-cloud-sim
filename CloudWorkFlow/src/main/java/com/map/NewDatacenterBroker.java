package com.map;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

import java.util.*;

/**
 * Created by Daniel on 15-01-2017.
 */
public class NewDatacenterBroker extends DatacenterBroker {
    public NewDatacenterBroker(String name) throws Exception {
        super(name);
    }

    private Integer getMinKey(Map<Integer, Integer> map, List<Integer> keys) {
        Integer minKey = null;
        int minValue = Integer.MAX_VALUE;
        for (Integer key : keys) {
            int value = map.get(key);
            if (value < minValue) {
                minValue = value;
                minKey = key;
            }
        }
        return minKey;
    }

    @Override
    protected void submitCloudlets() {
        NewCloudlet cloudlet = null;
        while (true) {
            int vmIndex = 0;
            ArrayList successfullySubmitted = new ArrayList();
            List<NewCloudlet> lc = this.getCloudletList();
            Iterator iterator = lc.iterator();
            boolean finished = true;
            while (iterator.hasNext()) {
                cloudlet = (NewCloudlet) iterator.next();
                Vm vm = null;
                if (cloudlet.getVmId() == -1) {
                    // cloudlets dependencies
                    if (!cloudlet.getExecAfterCloudlets().isEmpty()) {
                        finished = cloudlet.verifCloudlets();
                    }

                    if(finished == true){
                        vm = (Vm) this.getVmsCreatedList().get(vmIndex);
                    }
                    /*if(isFinished == false){
                        lc.remove(cloudlet.getCloudletId());
                        cloudlet.setCloudletId(0);
                        lc.add(cloudlet);
                    } else {
                        vm = (Vm) this.getVmsCreatedList().get(vmIndex);
                    }*/
                } else {
                    vm = VmList.getById(this.getVmsCreatedList(), cloudlet.getVmId());
                    if (vm == null) {
                        if (!Log.isDisabled()) {
                            Log.printConcatLine(new Object[]{Double.valueOf(CloudSim.clock()), ": ", this.getName(), ": Postponing execution of cloudlet ", Integer.valueOf(cloudlet.getCloudletId()), ": bount VM not available"});
                        }
                        continue;
                    }
                }
                if(finished == true) {
                    if (!Log.isDisabled()) {
                        Log.printConcatLine(new Object[]{Double.valueOf(CloudSim.clock()), ": ", this.getName(), ": Sending cloudlet ", Integer.valueOf(cloudlet.getCloudletId()), " to VM #", Integer.valueOf(vm.getId())});
                    }

                    cloudlet.setVmId(vm.getId());
                    this.sendNow(((Integer) this.getVmsToDatacentersMap().get(Integer.valueOf(vm.getId()))).intValue(), 21, cloudlet);
                    ++this.cloudletsSubmitted;
                    vmIndex = (vmIndex + 1) % this.getVmsCreatedList().size();
                    this.getCloudletSubmittedList().add(cloudlet);
                    successfullySubmitted.add(cloudlet);
                }
            }

            //SimEvent ev = new SimEvent(2, null, "Datacenter_0", "Broker", 32, null);
            //this.processVmCreate(ev);
            this.getCloudletList().removeAll(successfullySubmitted);
            //if(lc.isEmpty()){
                return;
            //}
        }
    }
}
