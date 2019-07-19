package io.clouditor.discovery;

import io.clouditor.util.PersistenceManager;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;

@Service
public class StorageAssetService {
  private static final Logger LOGGER = LogManager.getLogger();

  private Map<String, StorageAsset> storageAssets = new HashMap<>();

  public void updateStorageAsset(StorageAsset storageAsset) {
    this.storageAssets.put(storageAsset.getId(), storageAsset);
  }

  public Map<String, StorageAsset> getStorageAssets() {
    return storageAssets;
  }

  private void loadStorageAsset(StorageAsset storageAsset) {
    this.storageAssets.put(storageAsset.getId(), storageAsset);
  }

  public void modifyStorageAsset(StorageAsset storageAsset) {
    // load it
    this.loadStorageAsset(storageAsset);

    // persist it
    PersistenceManager.getInstance().persist(storageAsset);

    // update
    this.updateStorageAsset(storageAsset);
  }
}
