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
import model.Solve;

class SessionDBTest extends DatabaseTestBase {

    private CubeDB cubeDB;
    private SessionDB sessionDB;
    private SolveDB solveDB;
    private Cube testCube;

    @BeforeEach
    void setUp() {
        cubeDB = new CubeDB();
        sessionDB = new SessionDB();
        solveDB = new SolveDB();
        testCube = new Cube("3x3");
        cubeDB.insert(testCube);
    }

    @Test
    void insertSetsPersisted() {
        Session session = new Session(testCube.getId(), "Morning");
        sessionDB.insert(session);
        assertTrue(session.isPersisted());
    }

    @Test
    void insertSetsPositiveId() {
        Session session = new Session(testCube.getId(), "Morning");
        sessionDB.insert(session);
        assertTrue(session.getId() > 0);
    }

    @Test
    void insertAlreadyPersistedThrows() {
        Session session = new Session(testCube.getId(), "Morning");
        sessionDB.insert(session);
        assertThrows(IllegalArgumentException.class, () -> sessionDB.insert(session));
    }

    @Test
    void findByIdReturnsCorrectName() {
        Session session = new Session(testCube.getId(), "Morning");
        sessionDB.insert(session);
        Optional<Session> result = sessionDB.findById(session.getId());
        assertTrue(result.isPresent());
        assertEquals("Morning", result.get().getName());
    }

    @Test
    void findByIdReturnsCorrectId() {
        Session session = new Session(testCube.getId(), "Morning");
        sessionDB.insert(session);
        Optional<Session> result = sessionDB.findById(session.getId());
        assertTrue(result.isPresent());
        assertEquals(session.getId(), result.get().getId());
    }

    @Test
    void findByIdReturnsCorrectCubeId() {
        Session session = new Session(testCube.getId(), "Morning");
        sessionDB.insert(session);
        Optional<Session> result = sessionDB.findById(session.getId());
        assertTrue(result.isPresent());
        assertEquals(testCube.getId(), result.get().getCubeId());
    }

    @Test
    void findByIdSetsCreatedAt() {
        Session session = new Session(testCube.getId(), "Morning");
        sessionDB.insert(session);
        Optional<Session> result = sessionDB.findById(session.getId());
        assertTrue(result.isPresent());
        assertNotNull(result.get().getCreatedAt());
    }

    @Test
    void findByIdReturnsEmptyForMissingId() {
        Optional<Session> result = sessionDB.findById(999);
        assertFalse(result.isPresent());
    }

    @Test
    void findByCubeIdReturnsEmptyListWhenNoSessions() {
        List<Session> sessions = sessionDB.findByCubeId(testCube.getId());
        assertTrue(sessions.isEmpty());
    }

    @Test
    void findByCubeIdReturnsAllSessionsForCube() {
        sessionDB.insert(new Session(testCube.getId(), "Morning"));
        sessionDB.insert(new Session(testCube.getId(), "Afternoon"));
        sessionDB.insert(new Session(testCube.getId(), "Evening"));
        List<Session> sessions = sessionDB.findByCubeId(testCube.getId());
        assertEquals(3, sessions.size());
    }

    @Test
    void findByCubeIdReturnsInInsertionOrder() {
        sessionDB.insert(new Session(testCube.getId(), "Morning"));
        sessionDB.insert(new Session(testCube.getId(), "Afternoon"));
        sessionDB.insert(new Session(testCube.getId(), "Evening"));
        List<Session> sessions = sessionDB.findByCubeId(testCube.getId());
        assertEquals("Morning", sessions.get(0).getName());
        assertEquals("Afternoon", sessions.get(1).getName());
        assertEquals("Evening", sessions.get(2).getName());
    }

    @Test
    void findByCubeIdDoesNotReturnOtherCubesSessions() {
        Cube otherCube = new Cube("4x4");
        cubeDB.insert(otherCube);
        sessionDB.insert(new Session(testCube.getId(), "Morning"));
        sessionDB.insert(new Session(otherCube.getId(), "Afternoon"));
        List<Session> sessions = sessionDB.findByCubeId(testCube.getId());
        assertEquals(1, sessions.size());
        assertEquals("Morning", sessions.get(0).getName());
    }

    @Test
    void updateNamePersistsCorrectly() {
        Session session = new Session(testCube.getId(), "Morning");
        sessionDB.insert(session);
        sessionDB.updateName(session.getId(), "Evening");
        Optional<Session> result = sessionDB.findById(session.getId());
        assertTrue(result.isPresent());
        assertEquals("Evening", result.get().getName());
    }

    @Test
    void updateNameWithUnpersistedIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> sessionDB.updateName(-1, "Evening"));
    }

    @Test
    void deleteRemovesSession() {
        Session session = new Session(testCube.getId(), "Morning");
        sessionDB.insert(session);
        sessionDB.delete(session.getId());
        Optional<Session> result = sessionDB.findById(session.getId());
        assertFalse(result.isPresent());
    }

    @Test
    void deleteCascadesToSolves() {
        Session session = new Session(testCube.getId(), "Morning");
        sessionDB.insert(session);
        Solve solve = new Solve(session.getId(), 10000, "R U R' U'");
        solveDB.insert(solve);
        sessionDB.delete(session.getId());
        Optional<Solve> result = solveDB.findById(solve.getId());
        assertFalse(result.isPresent());
    }

    @Test
    void deleteCubeDeletesSession() {
        Session session = new Session(testCube.getId(), "Morning");
        sessionDB.insert(session);
        cubeDB.delete(testCube.getId());
        Optional<Session> result = sessionDB.findById(session.getId());
        assertFalse(result.isPresent());
    }
}