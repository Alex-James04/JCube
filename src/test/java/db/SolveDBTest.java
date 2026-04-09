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
import model.Penalty;
import model.Session;
import model.Solve;

class SolveDBTest extends DatabaseTestBase {

    private CubeDB cubeDB;
    private SessionDB sessionDB;
    private SolveDB solveDB;
    private Cube testCube;
    private Session testSession;

    @BeforeEach
    void setUp() {
        cubeDB = new CubeDB();
        sessionDB = new SessionDB();
        solveDB = new SolveDB();
        testCube = new Cube("3x3");
        cubeDB.insert(testCube);
        testSession = new Session(testCube.getId(), "Morning");
        sessionDB.insert(testSession);
    }

    @Test
    void insertSetsPersisted() {
        Solve solve = new Solve(testSession.getId(), 10000, "R U R' U'");
        solveDB.insert(solve);
        assertTrue(solve.isPersisted());
    }

    @Test
    void insertSetsPositiveId() {
        Solve solve = new Solve(testSession.getId(), 10000, "R U R' U'");
        solveDB.insert(solve);
        assertTrue(solve.getId() > 0);
    }

    @Test
    void insertAlreadyPersistedThrows() {
        Solve solve = new Solve(testSession.getId(), 10000, "R U R' U'");
        solveDB.insert(solve);
        assertThrows(IllegalArgumentException.class, () -> solveDB.insert(solve));
    }

    @Test
    void insertDefaultPenaltyIsNone() {
        Solve solve = new Solve(testSession.getId(), 10000, "R U R' U'");
        solveDB.insert(solve);
        Optional<Solve> result = solveDB.findById(solve.getId());
        assertTrue(result.isPresent());
        assertEquals(Penalty.NONE, result.get().getPenalty());
    }

    @Test
    void findByIdReturnsCorrectTimeMs() {
        Solve solve = new Solve(testSession.getId(), 10000, "R U R' U'");
        solveDB.insert(solve);
        Optional<Solve> result = solveDB.findById(solve.getId());
        assertTrue(result.isPresent());
        assertEquals(10000, result.get().getTimeMs());
    }

    @Test
    void findByIdReturnsCorrectSessionId() {
        Solve solve = new Solve(testSession.getId(), 10000, "R U R' U'");
        solveDB.insert(solve);
        Optional<Solve> result = solveDB.findById(solve.getId());
        assertTrue(result.isPresent());
        assertEquals(testSession.getId(), result.get().getSessionId());
    }

    @Test
    void findByIdReturnsCorrectScramble() {
        Solve solve = new Solve(testSession.getId(), 10000, "R U R' U'");
        solveDB.insert(solve);
        Optional<Solve> result = solveDB.findById(solve.getId());
        assertTrue(result.isPresent());
        assertEquals("R U R' U'", result.get().getScramble());
    }

    @Test
    void findByIdSetsCreatedAt() {
        Solve solve = new Solve(testSession.getId(), 10000, "R U R' U'");
        solveDB.insert(solve);
        Optional<Solve> result = solveDB.findById(solve.getId());
        assertTrue(result.isPresent());
        assertNotNull(result.get().getCreatedAt());
    }

    @Test
    void findByIdReturnsEmptyForMissingId() {
        Optional<Solve> result = solveDB.findById(999);
        assertFalse(result.isPresent());
    }

    @Test
    void findBySessionIdReturnsEmptyListWhenNoSolves() {
        List<Solve> solves = solveDB.findBySessionId(testSession.getId());
        assertTrue(solves.isEmpty());
    }

    @Test
    void findBySessionIdReturnsAllSolvesForSession() {
        solveDB.insert(new Solve(testSession.getId(), 10000, "R U R' U'"));
        solveDB.insert(new Solve(testSession.getId(), 12000, "F R U"));
        solveDB.insert(new Solve(testSession.getId(), 9000, "B L D"));
        List<Solve> solves = solveDB.findBySessionId(testSession.getId());
        assertEquals(3, solves.size());
    }

    @Test
    void findBySessionIdReturnsInInsertionOrder() {
        solveDB.insert(new Solve(testSession.getId(), 10000, "R U R' U'"));
        solveDB.insert(new Solve(testSession.getId(), 12000, "F R U"));
        solveDB.insert(new Solve(testSession.getId(), 9000, "B L D"));
        List<Solve> solves = solveDB.findBySessionId(testSession.getId());
        assertEquals(10000, solves.get(0).getTimeMs());
        assertEquals(12000, solves.get(1).getTimeMs());
        assertEquals(9000, solves.get(2).getTimeMs());
    }

    @Test
    void findBySessionIdDoesNotReturnOtherSessionsSolves() {
        Session otherSession = new Session(testCube.getId(), "Evening");
        sessionDB.insert(otherSession);
        solveDB.insert(new Solve(testSession.getId(), 10000, "R U R' U'"));
        solveDB.insert(new Solve(otherSession.getId(), 12000, "F R U"));
        List<Solve> solves = solveDB.findBySessionId(testSession.getId());
        assertEquals(1, solves.size());
        assertEquals(10000, solves.get(0).getTimeMs());
    }

    @Test
    void updatePenaltyToPlus2PersistsCorrectly() {
        Solve solve = new Solve(testSession.getId(), 10000, "R U R' U'");
        solveDB.insert(solve);
        solveDB.updatePenalty(solve.getId(), Penalty.PLUS2);
        Optional<Solve> result = solveDB.findById(solve.getId());
        assertTrue(result.isPresent());
        assertEquals(Penalty.PLUS2, result.get().getPenalty());
    }

    @Test
    void updatePenaltyToDNFPersistsCorrectly() {
        Solve solve = new Solve(testSession.getId(), 10000, "R U R' U'");
        solveDB.insert(solve);
        solveDB.updatePenalty(solve.getId(), Penalty.DNF);
        Optional<Solve> result = solveDB.findById(solve.getId());
        assertTrue(result.isPresent());
        assertEquals(Penalty.DNF, result.get().getPenalty());
    }

    @Test
    void updatePenaltyBackToNonePersistsCorrectly() {
        Solve solve = new Solve(testSession.getId(), 10000, "R U R' U'");
        solveDB.insert(solve);
        solveDB.updatePenalty(solve.getId(), Penalty.DNF);
        solveDB.updatePenalty(solve.getId(), Penalty.NONE);
        Optional<Solve> result = solveDB.findById(solve.getId());
        assertTrue(result.isPresent());
        assertEquals(Penalty.NONE, result.get().getPenalty());
    }

    @Test
    void updatePenaltyWithUnpersistedIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> solveDB.updatePenalty(-1, Penalty.DNF));
    }

    @Test
    void deleteRemovesSolve() {
        Solve solve = new Solve(testSession.getId(), 10000, "R U R' U'");
        solveDB.insert(solve);
        solveDB.delete(solve.getId());
        Optional<Solve> result = solveDB.findById(solve.getId());
        assertFalse(result.isPresent());
    }

    @Test
    void deleteAllRemovesAllSolvesForSession() {
        solveDB.insert(new Solve(testSession.getId(), 10000, "R U R' U'"));
        solveDB.insert(new Solve(testSession.getId(), 12000, "F R U"));
        solveDB.deleteAll(testSession.getId());
        assertEquals(0, solveDB.count(testSession.getId()));
    }

    @Test
    void deleteAllDoesNotAffectOtherSessionsSolves() {
        Session otherSession = new Session(testCube.getId(), "Evening");
        sessionDB.insert(otherSession);
        solveDB.insert(new Solve(testSession.getId(), 10000, "R U R' U'"));
        solveDB.insert(new Solve(otherSession.getId(), 12000, "F R U"));
        solveDB.deleteAll(testSession.getId());
        assertEquals(1, solveDB.count(otherSession.getId()));
    }

    @Test
    void countReturnsZeroWhenNoSolves() {
        assertEquals(0, solveDB.count(testSession.getId()));
    }

    @Test
    void countReturnsCorrectCount() {
        solveDB.insert(new Solve(testSession.getId(), 10000, "R U R' U'"));
        solveDB.insert(new Solve(testSession.getId(), 12000, "F R U"));
        assertEquals(2, solveDB.count(testSession.getId()));
    }

    @Test
    void countOnlyCountsOwnSession() {
        Session otherSession = new Session(testCube.getId(), "Evening");
        sessionDB.insert(otherSession);
        solveDB.insert(new Solve(testSession.getId(), 10000, "R U R' U'"));
        solveDB.insert(new Solve(otherSession.getId(), 12000, "F R U"));
        assertEquals(1, solveDB.count(testSession.getId()));
    }
}