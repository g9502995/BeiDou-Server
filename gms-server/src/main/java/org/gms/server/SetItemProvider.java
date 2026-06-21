package org.gms.server;

import org.gms.provider.Data;
import org.gms.provider.DataProvider;
import org.gms.provider.DataProviderFactory;
import org.gms.provider.DataTool;
import org.gms.provider.wz.WZFiles;

import java.util.*;

public class SetItemProvider {
    private static final SetItemProvider instance = new SetItemProvider();
    public static SetItemProvider getInstance() { return instance; }

    public static class SetItemEffect {
        public int count;
        public int incPAD, incMAD, incSTR, incDEX, incINT, incLUK, incMHP, incMMP;
        public int incPDD, incMDD, incACC, incEVA, incSpeed, incJump;
        public int incAllStat;
    }

    public static class SetItemInfo {
        public int setID;
        public String setName;
        public Set<Integer> itemIDs = new HashSet<>();
        public Map<Integer, SetItemEffect> effects = new HashMap<>();
    }

    private final Map<Integer, SetItemInfo> setInfos = new HashMap<>();
    private final Map<Integer, Integer> itemToSetMap = new HashMap<>();

    private SetItemProvider() {
        load();
    }

    private void load() {
        DataProvider etcData = DataProviderFactory.getDataProvider(WZFiles.ETC);
        Data setItemInfoData = etcData.getData("SetItemInfo.img");
        if (setItemInfoData == null) {
            System.out.println("Could not find SetItemInfo.img");
            return;
        }

        for (Data setNode : setItemInfoData.getChildren()) {
            int setID;
            try {
                setID = Integer.parseInt(setNode.getName());
            } catch (NumberFormatException e) {
                continue;
            }

            SetItemInfo info = new SetItemInfo();
            info.setID = setID;
            info.setName = DataTool.getString("setItemName", setNode, "");

            Data itemIDNode = setNode.getChildByPath("ItemID");
            if (itemIDNode != null) {
                for (Data itemNode : itemIDNode.getChildren()) {
                    int itemID = DataTool.getInt(itemNode, 0);
                    if (itemID > 0) {
                        info.itemIDs.add(itemID);
                        itemToSetMap.put(itemID, setID);
                    }
                }
            }

            Data effectNode = setNode.getChildByPath("Effect");
            if (effectNode != null) {
                for (Data effNode : effectNode.getChildren()) {
                    int count = Integer.parseInt(effNode.getName());
                    SetItemEffect eff = new SetItemEffect();
                    eff.count = count;
                    eff.incPAD = DataTool.getInt("incPAD", effNode, 0);
                    eff.incMAD = DataTool.getInt("incMAD", effNode, 0);
                    eff.incSTR = DataTool.getInt("incSTR", effNode, 0);
                    eff.incDEX = DataTool.getInt("incDEX", effNode, 0);
                    eff.incINT = DataTool.getInt("incINT", effNode, 0);
                    eff.incLUK = DataTool.getInt("incLUK", effNode, 0);
                    eff.incMHP = DataTool.getInt("incMHP", effNode, 0);
                    eff.incMMP = DataTool.getInt("incMMP", effNode, 0);
                    eff.incPDD = DataTool.getInt("incPDD", effNode, 0);
                    eff.incMDD = DataTool.getInt("incMDD", effNode, 0);
                    eff.incACC = DataTool.getInt("incACC", effNode, 0);
                    eff.incEVA = DataTool.getInt("incEVA", effNode, 0);
                    eff.incSpeed = DataTool.getInt("incSpeed", effNode, 0);
                    eff.incJump = DataTool.getInt("incJump", effNode, 0);
                    eff.incAllStat = DataTool.getInt("incAllStat", effNode, 0);
                    info.effects.put(count, eff);
                }
            }

            setInfos.put(setID, info);
        }
        System.out.println("Loaded " + setInfos.size() + " SetItemInfos");
    }

    public SetItemInfo getSetInfoByItemID(int itemID) {
        Integer setID = itemToSetMap.get(itemID);
        if (setID == null) return null;
        return setInfos.get(setID);
    }

    public List<SetItemEffect> getActiveEffects(Map<Integer, Integer> equippedSetCounts) {
        List<SetItemEffect> activeEffects = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : equippedSetCounts.entrySet()) {
            int setID = entry.getKey();
            int equippedCount = entry.getValue();
            SetItemInfo info = setInfos.get(setID);
            if (info != null) {
                for (Map.Entry<Integer, SetItemEffect> effEntry : info.effects.entrySet()) {
                    int requiredCount = effEntry.getKey();
                    if (equippedCount >= requiredCount) {
                        activeEffects.add(effEntry.getValue());
                    }
                }
            }
        }
        return activeEffects;
    }
    
    public int getSetID(int itemID) {
        return itemToSetMap.getOrDefault(itemID, -1);
    }
}
