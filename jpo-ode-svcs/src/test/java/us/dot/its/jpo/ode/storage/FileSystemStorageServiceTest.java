package us.dot.its.jpo.ode.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;

import mockit.Capturing;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import us.dot.its.jpo.ode.OdeProperties;
import us.dot.its.jpo.ode.eventlog.EventLogger;

import org.springframework.web.multipart.MultipartFile;

public class FileSystemStorageServiceTest {

    @Mocked
    OdeProperties mockOdeProperties;

    @Before
    public void setupOdePropertiesExpectations() {
        new Expectations() {
            {
                mockOdeProperties.getUploadLocationRoot();
                result = anyString;
                minTimes = 0;
                mockOdeProperties.getUploadLocationBsm();
                result = anyString;
                minTimes = 0;
                mockOdeProperties.getUploadLocationMessageFrame();
                result = anyString;
                minTimes = 0;
            }
        };
    }

    @Test
    public void shouldConstruct(@Mocked final Logger mockLogger, @Mocked LoggerFactory unused) {

//        new Expectations() {
//            {
//                LoggerFactory.getLogger(FileSystemStorageService.class);
//                result = mockLogger;
//            }
//        };

        FileSystemStorageService testFileSystemStorageService = new FileSystemStorageService(mockOdeProperties);

        assertNotNull(testFileSystemStorageService.getRootLocation());
        assertNotNull(testFileSystemStorageService.getBsmLocation());
        assertNotNull(testFileSystemStorageService.getMessageFrameLocation());

//        new Verifications() {
//            {
//                mockLogger.info(anyString, any);
//                times = 3;
//            }
//        };
    }

    @Test
    public void storeShouldThrowExceptionUnknownType(@Mocked MultipartFile mockMultipartFile) {

        String unknownType = "test123";

        try {
            new FileSystemStorageService(mockOdeProperties).store(mockMultipartFile, unknownType);
            fail("Expected StorageException");
        } catch (Exception e) {
            assertEquals("Incorrect exception thrown", StorageException.class, e.getClass());
            assertTrue("Incorrect message received", e.getMessage().startsWith("File type unknown:"));
        }

        new Verifications() {
            {
                EventLogger.logger.info(anyString, any, any);
            }
        };

    }

    @Test
    public void storeShouldTryToResolveBsmFilename(@Mocked MultipartFile mockMultipartFile) {

        String testType = "bsm";

        new Expectations() {
            {
                mockMultipartFile.getOriginalFilename();
                result = anyString;
                mockMultipartFile.isEmpty();
                result = true;
            }
        };

        try {
            new FileSystemStorageService(mockOdeProperties).store(mockMultipartFile, testType);
            fail("Expected StorageException");
        } catch (Exception e) {
            assertEquals("Incorrect exception thrown", StorageException.class, e.getClass());
            assertTrue("Incorrect message received", e.getMessage().startsWith("File is empty:"));
        }

        new Verifications() {
            {
                EventLogger.logger.info(anyString);
            }
        };
    }

    @Test
    public void storeShouldTryToResolveMessageFrameFilename(@Mocked MultipartFile mockMultipartFile) {

        String testType = "messageFrame";

        new Expectations() {
            {
                mockMultipartFile.getOriginalFilename();
                result = anyString;
                mockMultipartFile.isEmpty();
                result = true;
            }
        };

        try {
            new FileSystemStorageService(mockOdeProperties).store(mockMultipartFile, testType);
            fail("Expected StorageException");
        } catch (Exception e) {
            assertEquals("Incorrect exception thrown", StorageException.class, e.getClass());
            assertTrue("Incorrect message received", e.getMessage().startsWith("File is empty:"));
        }

        new Verifications() {
            {
                EventLogger.logger.info(anyString);
            }
        };
    }

    @Test
    public void storeShouldRethrowDeleteException(@Mocked MultipartFile mockMultipartFile, @Mocked Files unused) {

        String testType = "bsm";

        new Expectations() {
            {
                mockMultipartFile.getOriginalFilename();
                result = anyString;
                mockMultipartFile.isEmpty();
                result = false;
            }
        };

        try {
            new Expectations() {
                {
                    Files.deleteIfExists((Path) any);
                    result = new IOException("testException123");
                }
            };
        } catch (IOException e1) {
            fail("Unexpected exception on Files.deleteIfExists() expectation creation");
        }

        try {
            new FileSystemStorageService(mockOdeProperties).store(mockMultipartFile, testType);
            fail("Expected StorageException");
        } catch (Exception e) {
            assertEquals("Incorrect exception thrown", StorageException.class, e.getClass());
            assertTrue("Incorrect message received", e.getMessage().startsWith("Failed to delete existing file:"));
        }

        new Verifications() {
            {
                EventLogger.logger.info("Deleting existing file: {}", any);
                EventLogger.logger.info("Failed to delete existing file: {} ", any);
            }
        };
    }

    @Test
    public void storeShouldRethrowCopyException(@Mocked MultipartFile mockMultipartFile, @Mocked Files unusedFiles,
            @Mocked final Logger mockLogger, @Mocked LoggerFactory unusedLogger, @Mocked InputStream mockInputStream) {

        String testType = "bsm";

        try {
            new Expectations() {
                {
                    mockMultipartFile.getOriginalFilename();
                    result = anyString;
                    
                    mockMultipartFile.isEmpty();
                    result = false;
                    
                    mockMultipartFile.getInputStream();
                    result = mockInputStream;
                    
                    Files.deleteIfExists((Path) any);

                    Files.copy((InputStream) any, (Path) any);
                    result = new IOException("testException123");
                }
            };
        } catch (IOException e1) {
            fail("Unexpected exception creating test Expectations: " + e1);
        }
        
        try {
            new FileSystemStorageService(mockOdeProperties).store(mockMultipartFile, testType);
            fail("Expected StorageException");
        } catch (Exception e) {
            assertEquals("Incorrect exception thrown", StorageException.class, e.getClass());
            assertTrue("Incorrect message received",
                    e.getMessage().startsWith("Failed to store file in shared directory"));
        }

        new Verifications() {
            {
                EventLogger.logger.info("Copying file {} to {}", anyString, (Path) any);
                EventLogger.logger.info("Failed to store file in shared directory {}", (Path) any);
            }
        };
    }
    
    @Test
    public void loadAllShouldRethrowException(@Mocked Files unused) {
        try {
            new Expectations() {{
                Files.walk((Path) any, anyInt);//.filter(null).map(null);
                result = new IOException("testException123");
            }};
        } catch (IOException e) {
           fail("Unexpected exception creating Expectations: " + e);
        }
        
        try {
            new FileSystemStorageService(mockOdeProperties).loadAll();
            fail("Expected StorageException");
        } catch (Exception e) {
            assertEquals("Incorrect exception thrown", StorageException.class, e.getClass());
            assertTrue("Incorrect message received",
                    e.getMessage().startsWith("Failed to read files stored in"));
        }
        
        new Verifications() {{
            EventLogger.logger.info("Failed to read files stored in {}", (Path) any);
        }};
    }

}
