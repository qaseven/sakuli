/*
 * Sakuli - Testing and Monitoring-Tool for Websites and common UIs.
 *
 * Copyright 2013 - 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.consol.sakuli.services.impl;

import de.consol.sakuli.datamodel.TestSuite;
import de.consol.sakuli.datamodel.properties.SakuliProperties;
import de.consol.sakuli.datamodel.properties.TestSuiteProperties;
import de.consol.sakuli.datamodel.state.TestSuiteState;
import de.consol.sakuli.services.dao.DaoTestSuite;
import de.consol.sakuli.utils.TestSuitePropertiesTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * @author tschneck
 *         Date: 22.05.14
 */
public class InitializingServiceImplTest {

    @Spy
    private TestSuite ts;
    @Spy
    private TestSuiteProperties testSuiteProperties;
    @Mock
    private SakuliProperties sakuliProperties;
    @Mock
    private DaoTestSuite daoTestSuite;
    @InjectMocks
    private InitializingServiceImpl testling;

    @Test
    public void testInit() throws Throwable {

        testSuiteProperties = spy(TestSuitePropertiesTestUtils.getTestProps(this.getClass(), "valid", "suite_id"));
        ts = spy(new TestSuite(testSuiteProperties));
        MockitoAnnotations.initMocks(this);
        when(sakuliProperties.isPersistInDatabaseEnabled()).thenReturn(false);

        testling.initTestSuite();

        assertEquals(ts.getState(), TestSuiteState.RUNNING);
        assertNotNull(ts.getStartDate());
        assertTrue(ts.getAbsolutePathOfTestSuiteFile().endsWith("valid" + File.separator + "testsuite.suite"), "test absolut path");
        assertEquals(ts.getDbPrimaryKey(), -1);
        assertEquals(ts.getTestCases().size(), 1);
        assertEquals(ts.getId(), "suite_id");
        verify(daoTestSuite, never()).insertInitialTestSuiteData();
    }

    @Test
    public void testInitWithOutTestCases() throws Throwable {
        TestSuiteProperties props = new TestSuiteProperties();
        props.setTestSuiteId("suite_id");
        props.setLoadTestCasesAutomatic(false);
        testSuiteProperties = spy(props);
        ts = spy(new TestSuite(testSuiteProperties));
        MockitoAnnotations.initMocks(this);
        when(sakuliProperties.isPersistInDatabaseEnabled()).thenReturn(false);

        testling.initTestSuite();

        assertEquals(ts.getState(), TestSuiteState.RUNNING);
        assertNotNull(ts.getStartDate());
        assertNull(ts.getAbsolutePathOfTestSuiteFile());
        assertEquals(ts.getDbPrimaryKey(), -1);
        assertNull(ts.getTestCases());
        assertEquals(ts.getId(), "suite_id");
        verify(daoTestSuite, never()).insertInitialTestSuiteData();
    }

    @Test
    public void testInitWithDaoAction() throws Throwable {
        TestSuiteProperties props = new TestSuiteProperties();
        props.setTestSuiteId("suite_id");
        props.setLoadTestCasesAutomatic(false);

        testSuiteProperties = spy(props);
        ts = spy(new TestSuite(testSuiteProperties));
        MockitoAnnotations.initMocks(this);
        when(sakuliProperties.isPersistInDatabaseEnabled()).thenReturn(true);
        when(daoTestSuite.insertInitialTestSuiteData()).thenReturn(999);

        testling.initTestSuite();

        verify(daoTestSuite).insertInitialTestSuiteData();
        assertEquals(ts.getState(), TestSuiteState.RUNNING);
        assertNotNull(ts.getStartDate());
        assertNull(ts.getAbsolutePathOfTestSuiteFile());
        assertEquals(ts.getDbPrimaryKey(), 999);
        assertNull(ts.getTestCases());
        assertEquals(ts.getId(), "suite_id");

    }

    @Test(expectedExceptions = FileNotFoundException.class, expectedExceptionsMessageRegExp = "test case path \".*unValidTestCase.*\" doesn't exists - check your \"testsuite.suite\" file")
    public void testInitExceptionForTestCase() throws Throwable {
        testSuiteProperties = spy(TestSuitePropertiesTestUtils.getTestProps(this.getClass(), "unvalid", ""));
        ts = spy(new TestSuite(testSuiteProperties));
        MockitoAnnotations.initMocks(this);
        testling.initTestSuite();
    }

}