package com.orientechnologies.orient.core.sql.executor;

import com.orientechnologies.common.concur.OTimeoutException;
import com.orientechnologies.orient.core.command.OBasicCommandContext;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.sql.parser.OIdentifier;
import com.orientechnologies.orient.core.sql.parser.OLocalResultSet;
import com.orientechnologies.orient.core.sql.parser.OStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by luigidellaquila on 03/08/16.
 */
public class LetQueryStep extends AbstractExecutionStep {

  private final OIdentifier varName;
  private final OStatement  query;

  public LetQueryStep(OIdentifier varName, OStatement query, OCommandContext ctx) {
    super(ctx);
    this.varName = varName;
    this.query = query;
  }

  @Override public OTodoResultSet syncPull(OCommandContext ctx, int nRecords) throws OTimeoutException {
    if (!getPrev().isPresent()) {
      throw new OCommandExecutionException("Cannot execute a local LET on a query without a target");
    }
    return new OTodoResultSet() {
      OTodoResultSet source = getPrev().get().syncPull(ctx, nRecords);

      @Override public boolean hasNext() {
        return source.hasNext();
      }

      @Override public OResult next() {
        OResultInternal result = (OResultInternal) source.next();
        if (result != null) {
          calculate(result, ctx);
        }
        return result;
      }

      private void calculate(OResultInternal result, OCommandContext ctx) {
        OBasicCommandContext subCtx = new OBasicCommandContext();
        subCtx.setDatabase(ctx.getDatabase());
        subCtx.setParentWithoutOverridingChild(ctx);
        OInternalExecutionPlan subExecutionPlan = query.createExecutionPlan(subCtx);
        result.setProperty(varName.getStringValue(), toList(new OLocalResultSet(subExecutionPlan)));
      }

      private List<OResult> toList(OLocalResultSet oLocalResultSet) {
        List<OResult> result = new ArrayList<>();
        while (oLocalResultSet.hasNext()) {
          result.add(oLocalResultSet.next());
        }
        oLocalResultSet.close();
        return result;
      }

      @Override public void close() {
        source.close();
      }

      @Override public Optional<OExecutionPlan> getExecutionPlan() {
        return null;
      }

      @Override public Map<String, Object> getQueryStats() {
        return null;
      }
    };
  }

  @Override public void asyncPull(OCommandContext ctx, int nRecords, OExecutionCallback callback) throws OTimeoutException {

  }

  @Override public void sendResult(Object o, Status status) {

  }

  @Override public String prettyPrint(int depth, int indent) {
    String spaces = OExecutionStepInternal.getIndent(depth, indent);
    return spaces + "+ LET (for each record)\n" +
        spaces + "  " + varName + " = (" + query + ")";
  }
}