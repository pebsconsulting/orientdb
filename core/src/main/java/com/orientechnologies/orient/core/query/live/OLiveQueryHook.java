package com.orientechnologies.orient.core.query.live;

import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabaseListener;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.ORecordOperation;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by luigidellaquila on 16/03/15.
 */
public class OLiveQueryHook extends ODocumentHookAbstract implements ODatabaseListener {

  protected static Map<ODatabaseDocument, List<ORecordOperation>> pendingOps  = new ConcurrentHashMap<ODatabaseDocument, List<ORecordOperation>>();

  // protected static Map<OStorage, OLiveQueryQueueThread> queueThreads = new ConcurrentHashMap<OStorage, OLiveQueryQueueThread>();

  static OLiveQueryQueueThread                                    queueThread = new OLiveQueryQueueThread();
  static Object                                                   threadLock  = new Object();

  public OLiveQueryHook(ODatabaseDocumentTx db) {
    db.registerListener(this);
  }

  public static Integer subscribe(Integer token, OLiveQueryListener iListener) {
    synchronized (threadLock) {
      if (!queueThread.isAlive()) {
        // TODO copy old queues!
        queueThread = new OLiveQueryQueueThread();
        queueThread.start();
      }
    }

    return queueThread.subscribe(token, iListener);
  }

  public static void unsubscribe(Integer id) {
    try {
      synchronized (threadLock) {
        queueThread.unsubscribe(id);
      }
    } catch (Exception e) {
      OLogManager.instance().warn(OLiveQueryHook.class, "Error on unsubscribing client");
    }
  }

  @Override
  public void onCreate(ODatabase iDatabase) {

  }

  @Override
  public void onDelete(ODatabase iDatabase) {
    synchronized (pendingOps) {
      pendingOps.remove(iDatabase);
    }
  }

  @Override
  public void onOpen(ODatabase iDatabase) {

  }

  @Override
  public void onBeforeTxBegin(ODatabase iDatabase) {

  }

  @Override
  public void onBeforeTxRollback(ODatabase iDatabase) {

  }

  @Override
  public void onAfterTxRollback(ODatabase iDatabase) {
    synchronized (pendingOps) {
      pendingOps.remove(iDatabase);
    }
  }

  @Override
  public void onBeforeTxCommit(ODatabase iDatabase) {

  }

  @Override
  public void onAfterTxCommit(ODatabase iDatabase) {
    List<ORecordOperation> list;
    synchronized (pendingOps) {
      list = pendingOps.remove(iDatabase);
    }
    // TODO sync
    if (list != null) {
      for (ORecordOperation item : list) {
        queueThread.enqueue(item);
      }
    }
  }

  @Override
  public void onClose(ODatabase iDatabase) {
    synchronized (pendingOps) {
      pendingOps.remove(iDatabase);
    }
  }

  @Override
  public void onRecordAfterCreate(ODocument iDocument) {
    addOp(iDocument, ORecordOperation.CREATED);
  }

  @Override
  public void onRecordAfterUpdate(ODocument iDocument) {
    addOp(iDocument, ORecordOperation.UPDATED);
  }

  @Override
  public RESULT onRecordBeforeDelete(ODocument iDocument) {
    addOp(iDocument, ORecordOperation.DELETED);
    return RESULT.RECORD_NOT_CHANGED;
  }

  protected void addOp(ODocument iDocument, byte iType) {
    ODatabaseDocumentInternal db = ODatabaseRecordThreadLocal.INSTANCE.get();
    if (db.getTransaction() == null || !db.getTransaction().isActive()) {

      // TODO synchronize
      ORecordOperation op = new ORecordOperation(iDocument, iType);
      queueThread.enqueue(op);
      return;
    }
    ORecordOperation result = new ORecordOperation(iDocument, iType);
    synchronized (pendingOps) {
      List<ORecordOperation> list = this.pendingOps.get(db.getStorage());
      if (list == null) {
        list = new ArrayList<ORecordOperation>();
        this.pendingOps.put(db, list);
      }
      list.add(result);
    }
  }

  @Override
  public boolean onCorruptionRepairDatabase(ODatabase iDatabase, String iReason, String iWhatWillbeFixed) {
    return false;
  }

  @Override
  public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
    return DISTRIBUTED_EXECUTION_MODE.BOTH;
  }

  @Override
  public void onUnregister() {
    super.onUnregister();
    // for (OLiveQueryQueueThread queueThread : OLiveQueryHook.queueThreads.values()) {
    // queueThread.stopExecution();
    // }

  }
}
