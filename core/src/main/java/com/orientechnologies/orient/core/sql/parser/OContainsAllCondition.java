/* Generated By:JJTree: Do not edit this line. OContainsAllCondition.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.orientechnologies.orient.core.sql.parser;

import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.record.OIdentifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OContainsAllCondition extends OBooleanExpression {

  protected OExpression left;

  protected OExpression right;

  protected OOrBlock    rightBlock;

  public OContainsAllCondition(int id) {
    super(id);
  }

  public OContainsAllCondition(OrientSql p, int id) {
    super(p, id);
  }

  /** Accept the visitor. **/
  public Object jjtAccept(OrientSqlVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean evaluate(OIdentifiable currentRecord, OCommandContext ctx) {
    return false;// TODO
  }


  public void toString(Map<Object, Object> params, StringBuilder builder) {
    left.toString(params, builder);
    builder.append(" CONTAINSALL ");
    if (right != null) {
      right.toString(params, builder);
    } else if (rightBlock != null) {
      builder.append("(");
      rightBlock.toString(params, builder);
      builder.append(")");
    }
  }

  public OExpression getLeft() {
    return left;
  }

  public void setLeft(OExpression left) {
    this.left = left;
  }

  public OExpression getRight() {
    return right;
  }

  public void setRight(OExpression right) {
    this.right = right;
  }

  @Override
  public boolean supportsBasicCalculation() {
    if (left != null && !left.supportsBasicCalculation()) {
      return false;
    }
    if (right != null && !right.supportsBasicCalculation()) {
      return false;
    }
    if (rightBlock != null && !rightBlock.supportsBasicCalculation()) {
      return false;
    }
    return true;
  }

  @Override
  protected int getNumberOfExternalCalculations() {
    int total = 0;
    if (left != null && !left.supportsBasicCalculation()) {
      total++;
    }
    if (right != null && !right.supportsBasicCalculation()) {
      total++;
    }
    if (rightBlock != null && !rightBlock.supportsBasicCalculation()) {
      total++;
    }
    return total;
  }

  @Override
  protected List<Object> getExternalCalculationConditions() {
    List<Object> result = new ArrayList<Object>();
    if (left != null && !left.supportsBasicCalculation()) {
      result.add(left);
    }
    if (right != null && !right.supportsBasicCalculation()) {
      result.add(right);
    }
    if (rightBlock != null) {
      result.addAll(rightBlock.getExternalCalculationConditions());
    }
    return result;
  }
}
/* JavaCC - OriginalChecksum=ab7b4e192a01cda09a82d5b80ef4ec60 (do not edit this line) */
