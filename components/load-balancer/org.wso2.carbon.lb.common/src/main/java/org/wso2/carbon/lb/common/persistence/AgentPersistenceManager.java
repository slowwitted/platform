package org.wso2.carbon.lb.common.persistence;

import org.wso2.carbon.lb.common.dao.ContainerDAO;
import org.wso2.carbon.lb.common.dao.Ec2DAO;
import org.wso2.carbon.lb.common.dao.HostMachineDAO;
import org.wso2.carbon.lb.common.dao.InstanceDAO;
import org.wso2.carbon.lb.common.dao.ZoneDAO;
import org.wso2.carbon.lb.common.dto.Container;
import org.wso2.carbon.lb.common.dto.ContainerInformation;
import org.wso2.carbon.lb.common.dto.HostMachine;
import org.wso2.carbon.lb.common.dto.Zone;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *   This class is written to interface all the dao classes through a singleton class. All the access
 *   requests to database are gone through this class. Therefore there is a relevant method in this
 *   class for all the public methods in all the DAO classes.
 *
 */
public class    AgentPersistenceManager {
    private static AgentPersistenceManager manager = new AgentPersistenceManager();

    /**
         * Returning the agent persistence manager instance : singleton pattern
         *
         * @return manager
         */
    public static AgentPersistenceManager getPersistenceManager()  {
        return manager;
    }

    public boolean addHostMachine(HostMachine hostMachine, String[] domains) throws SQLException {
        HostMachineDAO hostMachineDAO = new HostMachineDAO();
        return hostMachineDAO.create(hostMachine, domains);
    }
    
    public void changeHostMachineAvailability(String endPoint, boolean availability)
            throws SQLException {
        HostMachineDAO hostMachineDAO =  new HostMachineDAO();
        hostMachineDAO.changeAvailability(endPoint, availability);
    }
    public boolean addZone(Zone zone, String[] domains)
            throws SQLException {
        ZoneDAO zoneResourcePlanDAO = new ZoneDAO();
        return zoneResourcePlanDAO.create(zone, domains);
    }

    public void addContainer(Container container)
            throws SQLException {
        ContainerDAO containerDAO = new ContainerDAO();
        containerDAO.create(container);
    }

    public void deleteContainer(String containerName)
            throws SQLException {
        ContainerDAO containerDAO = new ContainerDAO();
        containerDAO.delete(containerName);
    }

    public boolean deleteHostMachine(String epr)
            throws SQLException {
        HostMachineDAO hostMachineDAO = new HostMachineDAO();
        return hostMachineDAO.delete(epr);
    }

    public void changeContainerState(String containerName, Boolean state)
            throws SQLException {
        ContainerDAO containerDAO = new ContainerDAO();
        containerDAO.changeState(containerName, state);
    }
    public ContainerInformation retrieveAvailableContainerInformation(String domain)
            throws SQLException {
        ContainerDAO containerDAO = new ContainerDAO();
        return containerDAO.retrieveAvailableContainerInformation(domain);
    }

    public boolean isZoneExist(String zone) throws SQLException {
        ZoneDAO zoneDAO = new ZoneDAO();
        return zoneDAO.isZoneExist(zone);
    }
    public boolean isDomainExist(String domain) throws SQLException {
        ZoneDAO zoneDAO = new ZoneDAO();
        return zoneDAO.isDomainExist(domain);
    }


    public boolean updateDomainConfigs(String zone, String[] domains) throws SQLException {
        ZoneDAO zoneDAO = new ZoneDAO();
        return zoneDAO.update(zone, domains);
    }
    public boolean isHostMachineExist(String endPoint) throws SQLException {
        HostMachineDAO hostMachineDAO = new HostMachineDAO();
        return hostMachineDAO.isHostMachineExist(endPoint);
    }
    public boolean isHostMachinesAvailableInDomain(String domain) throws SQLException {
        HostMachineDAO hostMachineDAO = new HostMachineDAO();
        return hostMachineDAO.isAvailableInDomain(domain);
    }

    public HashMap<String, ArrayList<String>> retrieveDomainToInstanceIdsMap() throws SQLException {
        InstanceDAO instanceDAO = new InstanceDAO();
        return instanceDAO.getDomainToInstanceIdsMap();
    }


    public HashMap<String, String> retrieveInstanceIdToAdapterMap() throws SQLException {
        InstanceDAO instanceDAO = new InstanceDAO();
        return instanceDAO.getInstanceIdToAdapterMap();
    }

    public HashMap<String, String> retrieveContainerIdToAgentMap() throws SQLException {
        HostMachineDAO hostMachineDAO = new HostMachineDAO();
        return hostMachineDAO.getContainerIdToAgentMap();
    }

    public HashMap<String, String> retrieveAgentToContainerRootMap() throws SQLException {
        HostMachineDAO hostMachineDAO = new HostMachineDAO();
        return hostMachineDAO.getAgentToContainerRootMap();
    }

    public boolean addInstance(String instanceId, String adapter, String domain)
            throws SQLException {
        InstanceDAO instanceDAO = new InstanceDAO();
        return instanceDAO.add(instanceId, adapter, domain);
    }


    public HashMap<String, String> retrieveUuidToEc2IdMap() throws SQLException {
        Ec2DAO ec2DAO = new Ec2DAO();
        return ec2DAO.getUuidToEc2IdMap();
    }

    public boolean addEc2Instance(String uuid, String ec2Id)
            throws SQLException {
        Ec2DAO ec2DAO = new Ec2DAO();
        return ec2DAO.add(uuid, ec2Id);
    }


}
