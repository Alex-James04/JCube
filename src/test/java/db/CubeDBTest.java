package db;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Cube;
import model.Session;

class CubeDBTest extends DatabaseTestBase {

    private CubeDB cubeDB;
    private SessionDB sessionDB;

    @BeforeEach
    void setUp() {
        cubeDB = new CubeDB();
        sessionDB = new SessionDB();
    }

    @Test
    void insertSetsPersisted() {
        Cube cube = new Cube("3x3");
        cubeDB.insert(cube);
        assertTrue(cube.isPersisted());
    }

    @Test
    void insertSetsPositiveId() {
        Cube cube = new Cube("3x3");
        cubeDB.insert(cube);
        assertTrue(cube.getId() > 0);
    }

    @Test
    void insertAlreadyPersistedThrows() {
        Cube cube = new Cube("3x3");
        cubeDB.insert(cube);
        assertThrows(IllegalArgumentException.class, () -> cubeDB.insert(cube));
    }

    @Test
    void findByIdReturnsCorrectName() {
        Cube cube = new Cube("3x3");
        cubeDB.insert(cube);
        Optional<Cube> result = cubeDB.findById(cube.getId());
        assertTrue(result.isPresent());
        assertEquals("3x3", result.get().getName());
    }

    @Test
    void findByIdReturnsCorrectId() {
        Cube cube = new Cube("3x3");
        cubeDB.insert(cube);
        Optional<Cube> result = cubeDB.findById(cube.getId());
        assertTrue(result.isPresent());
        assertEquals(cube.getId(), result.get().getId());
    }

    @Test
    void findByIdSetsCreatedAt() {
        Cube cube = new Cube("3x3");
        cubeDB.insert(cube);
        Optional<Cube> result = cubeDB.findById(cube.getId());
        assertTrue(result.isPresent());
        assertNotNull(result.get().getDateTimeCreated());
    }

    @Test
    void findByIdReturnsEmptyForMissingId() {
        Optional<Cube> result = cubeDB.findById(999);
        assertFalse(result.isPresent());
    }

    @Test
    void findAllReturnsEmptyListWhenNoCubes() {
        List<Cube> cubes = cubeDB.findAll();
        assertTrue(cubes.isEmpty());
    }

    @Test
    void findAllReturnsAllInsertedCubes() {
        cubeDB.insert(new Cube("3x3"));
        cubeDB.insert(new Cube("4x4"));
        cubeDB.insert(new Cube("Pyraminx"));
        List<Cube> cubes = cubeDB.findAll();
        assertEquals(3, cubes.size());
    }

    @Test
    void findAllReturnsInInsertionOrder() {
        cubeDB.insert(new Cube("3x3"));
        cubeDB.insert(new Cube("4x4"));
        cubeDB.insert(new Cube("Pyraminx"));
        List<Cube> cubes = cubeDB.findAll();
        assertEquals("3x3", cubes.get(0).getName());
        assertEquals("4x4", cubes.get(1).getName());
        assertEquals("Pyraminx", cubes.get(2).getName());
    }

    @Test
    void updateNamePersistsCorrectly() {
        Cube cube = new Cube("3x3");
        cubeDB.insert(cube);
        cubeDB.updateName(cube.getId(), "3x3 Speed");
        Optional<Cube> result = cubeDB.findById(cube.getId());
        assertTrue(result.isPresent());
        assertEquals("3x3 Speed", result.get().getName());
    }

    @Test
    void updateNameWithUnpersistedIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> cubeDB.updateName(-1, "3x3"));
    }

    @Test
    void deleteRemovesCube() {
        Cube cube = new Cube("3x3");
        cubeDB.insert(cube);
        cubeDB.delete(cube.getId());
        Optional<Cube> result = cubeDB.findById(cube.getId());
        assertFalse(result.isPresent());
    }

    @Test
    void deleteCascadesToSessions() {
        Cube cube = new Cube("3x3");
        cubeDB.insert(cube);
        Session session = new Session(cube.getId(), "Morning");
        sessionDB.insert(session);
        cubeDB.delete(cube.getId());
        Optional<Session> result = sessionDB.findById(session.getId());
        assertFalse(result.isPresent());
    }
}