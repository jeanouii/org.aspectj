/********************************************************************
 * Copyright (c) 2010 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement (SpringSource)         initial implementation
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import junit.framework.Assert;

import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.internal.AspectJElementHierarchy;

/**
 * Incremental compilation tests. MultiProjectIncrementalTests was getting unwieldy - started this new test class for 1.6.10.
 * 
 * @author Andy Clement
 * @since 1.6.10
 */
public class IncrementalCompilationTests extends AbstractMultiProjectIncrementalAjdeInteractionTestbed {

	/**
	 * Build a pair of files, then change the throws clause in the first one (add a throws clause where there wasnt one). The second
	 * file should now have a 'unhandled exception' error on it.
	 */
	public void testModifiedThrowsClauseShouldTriggerError_318884() throws Exception {
		String p = "pr318884_1";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("B.java:4:0::0 Unhandled exception type IOException", getErrorMessages(p).get(0));
	}

	/**
	 * Build a pair of files, then change the throws clause in the first one (change the type of the thrown exception). The second
	 * file should now have a 'unhandled exception' error on it.
	 */
	public void testModifiedThrowsClauseShouldTriggerError_318884_2() throws Exception {
		String p = "pr318884_2";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("B.java:4:0::0 Unhandled exception type Exception", getErrorMessages(p).get(0));
	}

	// changing method return type parameterization
	public void testModifiedMethodReturnTypeGenericTypeParameterShouldTriggerError_318884_3() throws Exception {
		String p = "pr318884_3";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("The return type is incompatible with B.foo()", getErrorMessages(p).get(0));
	}

	// changing method parameter type parameterization
	public void testModifiedMethodParameterGenericTypeParameterShouldTriggerError_318884_4() throws Exception {
		String p = "pr318884_4";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains(
				"Name clash: The method foo(List<String>) of type A has the same erasure as foo(List<Integer>) of type B but does not override it",
				getErrorMessages(p).get(0));
	}

	// changing constructor parameter type parameterization
	public void testModifiedConstructorParameterGenericTypeParameterShouldTriggerError_318884_5() throws Exception {
		String p = "pr318884_5";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("The constructor B(List<String>) is undefined", getErrorMessages(p).get(0));
	}

	// changing field type parameterization
	public void testModifiedFieldTypeGenericTypeParameterShouldTriggerError_318884_6() throws Exception {
		String p = "pr318884_6";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("Type mismatch: cannot convert from element type Integer to String", getErrorMessages(p).get(0));
	}

	// removing static inner class
	public void testInnerClassChanges_318884_7() throws Exception {
		String p = "pr318884_7";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 3);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("B.C cannot be resolved to a type", getErrorMessages(p).get(0));
	}

	// removing constructor from a static inner class
	public void testInnerClassChanges_318884_9() throws Exception {
		String p = "pr318884_9";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 3);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("The constructor B.C(String) is undefined", getErrorMessages(p).get(0));
	}

	// removing class
	public void testInnerClassChanges_318884_10() throws Exception {
		String p = "pr318884_10";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(2, getErrorMessages(p).size());
		assertContains("B cannot be resolved to a type", getErrorMessages(p).get(0));
	}

	// deleting unaffected model entries
	public void testDeletion_278496() throws Exception {
		String p = "PR278496_1";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:minimalModel=true");
		build(p);
		checkWasFullBuild();
		printModel(p);
		// Here is the model without deletion.  The node for 'Code.java' can safely be deleted as it contains
		// no types that are the target of relationships.
		//		PR278496_1  [build configuration file]  hid:=PR278496_1
		//		  a.b.c  [package]  hid:=PR278496_1<a.b.c
		//		    Azpect.java  [java source file] 1 hid:=PR278496_1<a.b.c{Azpect.java
		//		      a.b.c  [package declaration] 1 hid:=PR278496_1<a.b.c{Azpect.java%a.b.c
		//		        [import reference]  hid:=PR278496_1<a.b.c{Azpect.java#
		//		      Azpect  [aspect] 3 hid:=PR278496_1<a.b.c{Azpect.java}Azpect
		//		        before(): <anonymous pointcut>  [advice] 4 hid:=PR278496_1<a.b.c{Azpect.java}Azpect&before
		//		    Code.java  [java source file] 1 hid:=PR278496_1<a.b.c{Code.java
		//		      a.b.c  [package declaration] 1 hid:=PR278496_1<a.b.c{Code.java%a.b.c
		//		        [import reference]  hid:=PR278496_1<a.b.c{Code.java#
		//		        java.util.ArrayList  [import reference] 3 hid:=PR278496_1<a.b.c{Code.java#java.util.ArrayList
		//		        java.util.List  [import reference] 2 hid:=PR278496_1<a.b.c{Code.java#java.util.List
		//		      Code  [class] 5 hid:=PR278496_1<a.b.c{Code.java[Code
		//		        m()  [method] 6 hid:=PR278496_1<a.b.c{Code.java[Code~m
		//		    Code2.java  [java source file] 1 hid:=PR278496_1<a.b.c{Code2.java
		//		      a.b.c  [package declaration] 1 hid:=PR278496_1<a.b.c{Code2.java%a.b.c
		//		        [import reference]  hid:=PR278496_1<a.b.c{Code2.java#
		//		        java.util.ArrayList  [import reference] 3 hid:=PR278496_1<a.b.c{Code2.java#java.util.ArrayList
		//		        java.util.List  [import reference] 2 hid:=PR278496_1<a.b.c{Code2.java#java.util.List
		//		      Code2  [class] 5 hid:=PR278496_1<a.b.c{Code2.java[Code2
		//		        m()  [method] 6 hid:=PR278496_1<a.b.c{Code2.java[Code2~m
		//		Hid:1:(targets=1) =PR278496_1<a.b.c{Azpect.java}Azpect&before (advises) =PR278496_1<a.b.c{Code2.java[Code2
		//		Hid:2:(targets=1) =PR278496_1<a.b.c{Code2.java[Code2 (advised by) =PR278496_1<a.b.c{Azpect.java}Azpect&before

		// deletion turned on:
		//		PR278496_1  [build configuration file]  hid:=PR278496_1
		//		  a.b.c  [package]  hid:<a.b.c
		//		    Azpect.java  [java source file] 1 hid:<a.b.c{Azpect.java
		//		      a.b.c  [package declaration] 1 hid:<a.b.c{Azpect.java%a.b.c
		//		        [import reference]  hid:<a.b.c{Azpect.java#
		//		      Azpect  [aspect] 3 hid:<a.b.c{Azpect.java}Azpect
		//		        before(): <anonymous pointcut>  [advice] 4 hid:<a.b.c{Azpect.java}Azpect&before
		//		    Code2.java  [java source file] 1 hid:<a.b.c{Code2.java
		//		      a.b.c  [package declaration] 1 hid:<a.b.c{Code2.java%a.b.c
		//		        [import reference]  hid:<a.b.c{Code2.java#
		//		        java.util.ArrayList  [import reference] 3 hid:<a.b.c{Code2.java#java.util.ArrayList
		//		        java.util.List  [import reference] 2 hid:<a.b.c{Code2.java#java.util.List
		//		      Code2  [class] 5 hid:<a.b.c{Code2.java[Code2
		//		        m()  [method] 6 hid:<a.b.c{Code2.java[Code2~m
		//		Hid:1:(targets=1) <a.b.c{Azpect.java}Azpect&before (advises) <a.b.c{Code2.java[Code2
		//		Hid:2:(targets=1) <a.b.c{Code2.java[Code2 (advised by) <a.b.c{Azpect.java}Azpect&before

		AspectJElementHierarchy model = (AspectJElementHierarchy) getModelFor(p).getHierarchy();
		// Node for "Code.java" should not be there:
		IProgramElement ipe = model.findElementForHandleOrCreate("=PR278496_1<a.b.c{Code.java",false);
		Assert.assertNull(ipe);
	}
}