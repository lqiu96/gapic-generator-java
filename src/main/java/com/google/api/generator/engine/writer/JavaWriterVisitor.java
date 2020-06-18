// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.generator.engine.writer;

import com.google.api.generator.engine.ast.AssignmentExpr;
import com.google.api.generator.engine.ast.AstNodeVisitor;
import com.google.api.generator.engine.ast.BlockComment;
import com.google.api.generator.engine.ast.IdentifierNode;
import com.google.api.generator.engine.ast.JavaDocComment;
import com.google.api.generator.engine.ast.LineComment;
import com.google.api.generator.engine.ast.ReferenceTypeNode;
import com.google.api.generator.engine.ast.ScopeNode;
import com.google.api.generator.engine.ast.TypeNode;
import com.google.api.generator.engine.ast.TypeNode.TypeKind;
import com.google.api.generator.engine.ast.ValueExpr;
import com.google.api.generator.engine.ast.Variable;
import com.google.api.generator.engine.ast.VariableExpr;
import com.google.googlejavaformat.java.Formatter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class JavaWriterVisitor implements AstNodeVisitor {
  private static final String SPACE = " ";

  private final StringBuffer buffer = new StringBuffer();

  public JavaWriterVisitor() {}

  public void clear() {
    buffer.setLength(0);
  }

  public String write() {
    return buffer.toString();
  }

  @Override
  public void visit(IdentifierNode identifier) {
    buffer.append(identifier.name());
  }

  @Override
  public void visit(TypeNode type) {
    TypeKind typeKind = type.typeKind();
    StringBuilder generatedCodeBuilder = new StringBuilder();
    if (type.isPrimitiveType()) {
      generatedCodeBuilder.append(typeKind.toString().toLowerCase());
    } else {
      // A null pointer exception will be thrown if reference is null, which is WAI.
      // TODO(miraleung): Clean this up.
      visit(type.reference());
      return;
    }

    if (type.isArray()) {
      generatedCodeBuilder.append("[]");
    }

    buffer.append(generatedCodeBuilder.toString());
  }

  @Override
  public void visit(ScopeNode scope) {
    buffer.append(scope.toString());
  }

  @Override
  public void visit(ReferenceTypeNode reference) {
    throw new RuntimeException("Not yet implemented for reference types");
  }

  /** =============================== EXPRESSIONS =============================== */
  @Override
  public void visit(ValueExpr valueExpr) {
    buffer.append(valueExpr.value().toString());
  }

  @Override
  public void visit(VariableExpr variableExpr) {
    Variable variable = variableExpr.variable();
    TypeNode type = variable.type();
    ScopeNode scope = variableExpr.scope();

    if (variableExpr.isDecl()) {
      if (!scope.equals(ScopeNode.LOCAL)) {
        scope.accept(this);
        space();
      }

      if (variableExpr.isStatic()) {
        buffer.append("static");
        space();
      }

      if (variableExpr.isFinal()) {
        buffer.append("final");
        space();
      }

      type.accept(this);
      space();
    }

    variable.identifier().accept(this);
  }

  @Override
  public void visit(AssignmentExpr assignmentExpr) {
    assignmentExpr.variableExpr().accept(this);
    buffer.append(" = ");
    assignmentExpr.valueExpr().accept(this);
  }

  private void space() {
    buffer.append(SPACE);
  }

  /** =============================== STATEMENT =============================== */
  public String visit(LineComment linecomment){
      // Split comments by new line and add `//` to each line.
      String[] sourceStrings = linecomment.comment().split("\\r?\\n");
      for (int i = 0; i < sourceStrings.length; i++) {
        sourceStrings[i] = "// " + sourceStrings[i];
      }
      String formattedSource =format(String.join("\n", sourceStrings));
      return formattedSource;
  }

  public String visit(BlockComment blockComment){
    // Split comments by new line and embrace the comment block with `/** */`.
    String sourceString = blockComment.comment();
    String formattedSource = format("/** " + sourceString + " */");
    return formattedSource;
  }

  public String visit(JavaDocComment javaDocComment) {
    StringBuilder formattedComment = new StringBuilder("/**\n");
    ImmutableList<String> commentList = javaDocComment.comments();
    ImmutableMap<String, String> paramList = javaDocComment.params();
    ImmutableList<String> sampleCodeList = javaDocComment.sampleCode();
    for (String comment : commentList) {
      formattedComment.append("* " + comment + "\n");
    }
    for (String name : paramList.keySet()) {
      formattedComment.append("* @param " + name + " " + paramList.get(name) + "\n");
    }
    for (String sample : sampleCodeList) {
      formattedComment.append("* Sample code:\n* <pre><code>\n");
      String[] sampleLines = sample.split("\\r?\\n");
      for (int i = 0; i < sampleLines.length; i++) {
        sampleLines[i] = "* " + sampleLines[i];
      }
      formattedComment.append(String.join("\n", sampleLines) + "\n* </code></pre>\n");
    }
    if (javaDocComment.deprecated().isPresent()) {
      formattedComment.append("* @deprecated " + javaDocComment.deprecated() + "\n");
    }
    if (javaDocComment.throwsText().isPresent()) {
      formattedComment.append("* @throws " + javaDocComment.throwsText() + "\n");
    }
    String formattedSource = format(formattedComment.append("*/").toString());
    return formattedSource;
  }

  public String format(String comment) {
    String formattedComment = "";
    try{
      formattedComment = new Formatter().formatSource(comment);
    }catch(Exception e) {
      System.out.println(e.getMessage());
    }
    return formattedComment;
  }
}
