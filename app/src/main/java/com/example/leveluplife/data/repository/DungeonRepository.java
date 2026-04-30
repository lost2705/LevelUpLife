package com.example.leveluplife.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.leveluplife.data.dao.DungeonStateDao;
import com.example.leveluplife.data.dao.PlayerDao;
import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.entity.DungeonState;
import com.example.leveluplife.data.entity.Player;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DungeonRepository {

    private static final long DUNGEON_COOLDOWN_MS = 12 * 60 * 60 * 1000L;

    private final DungeonStateDao dungeonStateDao;
    private final PlayerDao playerDao;
    private final PlayerRepository playerRepository;
    private final ExecutorService executor;
    private final LiveData<DungeonState> dungeonState;
    private final MutableLiveData<String> battleLog = new MutableLiveData<>("");
    private final Random random = new Random();

    public DungeonRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.dungeonStateDao = db.dungeonStateDao();
        this.playerDao = db.playerDao();
        this.playerRepository = PlayerRepository.getInstance(application);
        this.executor = Executors.newSingleThreadExecutor();
        this.dungeonState = dungeonStateDao.getDungeonState();
    }

    public LiveData<DungeonState> getDungeonState() {
        return dungeonState;
    }

    public LiveData<String> getBattleLog() {
        return battleLog;
    }

    public void ensureDungeonStateExists() {
        executor.execute(() -> {
            DungeonState state = dungeonStateDao.getDungeonStateSync();
            Player player = playerDao.getPlayerSync();

            if (state == null) {
                state = new DungeonState();
                state.setId(1);
                state.setStatus("IDLE");
            }

            if (player != null) {
                state.setPlayerCurrentHp(player.getCurrentHp());
                state.setPlayerCurrentMana(player.getCurrentMana());
            }

            if (state.getEnemyName() == null) state.setEnemyName("");
            dungeonStateDao.insertOrReplace(state);
        });
    }

    public void startDungeonRun() {
        executor.execute(() -> {
            DungeonState state = getOrCreateState();
            Player player = playerDao.getPlayerSync();

            if (player == null) {
                appendLog("Player not found.");
                return;
            }

            long now = System.currentTimeMillis();
            if (state.getCooldownUntil() > now) {
                appendLog("Dungeon is on cooldown.");
                return;
            }

            state.setStatus("IN_PROGRESS");
            state.setStartedAt(now);
            state.setFinishedAt(0L);
            state.setCooldownUntil(0L);

            state.setPlayerCurrentHp(Math.max(1, player.getCurrentHp()));
            state.setPlayerCurrentMana(Math.max(0, player.getCurrentMana()));

            state.setEnemyName("Stone Golem");
            state.setEnemyMaxHp(calculateEnemyMaxHp(player));
            state.setEnemyCurrentHp(state.getEnemyMaxHp());

            state.setTurnNumber(1);
            state.setRewardXp(calculateRewardXp(player));
            state.setRewardGold(calculateRewardGold(player));

            clearLog();
            dungeonStateDao.insertOrReplace(state);
            appendLog("A Stone Golem emerges from the darkness.");
        });
    }

    public void playerAttack() {
        executor.execute(() -> {
            DungeonState state = dungeonStateDao.getDungeonStateSync();
            Player player = playerDao.getPlayerSync();

            if (!isBattleReady(state, player)) return;

            int damage = calculatePlayerAttackDamage(player);
            int enemyHp = Math.max(0, state.getEnemyCurrentHp() - damage);
            state.setEnemyCurrentHp(enemyHp);

            if (enemyHp <= 0) {
                finishVictory(state, "You attack for " + damage + " damage and defeat the Stone Golem.");
                return;
            }

            dungeonStateDao.update(state);
            appendLog("You attack for " + damage + " damage.");
            enemyTurn(state, player);
        });
    }

    public void playerSkill() {
        executor.execute(() -> {
            DungeonState state = dungeonStateDao.getDungeonStateSync();
            Player player = playerDao.getPlayerSync();

            if (!isBattleReady(state, player)) return;

            int manaCost = 15;
            if (state.getPlayerCurrentMana() < manaCost) {
                appendLog("Not enough mana.");
                return;
            }

            state.setPlayerCurrentMana(state.getPlayerCurrentMana() - manaCost);

            int damage = calculatePlayerSkillDamage(player);
            int enemyHp = Math.max(0, state.getEnemyCurrentHp() - damage);
            state.setEnemyCurrentHp(enemyHp);

            if (enemyHp <= 0) {
                finishVictory(state, "You cast a skill for " + damage + " damage and destroy the Stone Golem.");
                return;
            }

            dungeonStateDao.update(state);
            appendLog("You cast a skill for " + damage + " damage. Mana -" + manaCost + ".");
            enemyTurn(state, player);
        });
    }

    public void playerRest() {
        executor.execute(() -> {
            DungeonState state = dungeonStateDao.getDungeonStateSync();
            Player player = playerDao.getPlayerSync();

            if (!isBattleReady(state, player)) return;

            int hpRecover = 12 + random.nextInt(10);
            int manaRecover = 8 + random.nextInt(8);

            int recoveredHp = Math.min(player.getMaxHp(), state.getPlayerCurrentHp() + hpRecover);
            int recoveredMana = Math.min(player.getMaxMana(), state.getPlayerCurrentMana() + manaRecover);

            state.setPlayerCurrentHp(recoveredHp);
            state.setPlayerCurrentMana(recoveredMana);

            dungeonStateDao.update(state);
            appendLog("You rest by the campfire. HP +" + hpRecover + ", Mana +" + manaRecover + ".");
            enemyTurn(state, player);
        });
    }

    public void abandonRun() {
        executor.execute(() -> {
            DungeonState state = getOrCreateState();
            long now = System.currentTimeMillis();

            state.setStatus("COOLDOWN");
            state.setFinishedAt(now);
            state.setCooldownUntil(now + DUNGEON_COOLDOWN_MS);
            state.setEnemyName("");
            state.setEnemyCurrentHp(0);
            state.setEnemyMaxHp(0);
            state.setTurnNumber(1);
            state.setRewardXp(0);
            state.setRewardGold(0);

            dungeonStateDao.update(state);
            appendLog("You abandon the run. The dungeon seals itself for a time.");
        });
    }

    public void resetCooldownIfExpired() {
        executor.execute(() -> {
            DungeonState state = getOrCreateState();
            long now = System.currentTimeMillis();

            if ("COOLDOWN".equals(state.getStatus()) && state.getCooldownUntil() <= now) {
                state.setStatus("IDLE");
                state.setCooldownUntil(0L);
                state.setFinishedAt(0L);
                state.setStartedAt(0L);
                state.setEnemyName("");
                state.setEnemyCurrentHp(0);
                state.setEnemyMaxHp(0);
                state.setRewardXp(0);
                state.setRewardGold(0);
                state.setTurnNumber(1);
                dungeonStateDao.update(state);
                appendLog("The dungeon is available again.");
            }
        });
    }

    public void resetDungeonCooldown() {
        executor.execute(() -> {
            DungeonState state = getOrCreateState();

            state.setCooldownUntil(0L);
            if (!"IN_PROGRESS".equals(state.getStatus())) {
                state.setStatus("IDLE");
                state.setFinishedAt(0L);
                state.setStartedAt(0L);
                state.setEnemyName("");
                state.setEnemyCurrentHp(0);
                state.setEnemyMaxHp(0);
                state.setRewardXp(0);
                state.setRewardGold(0);
                state.setTurnNumber(1);
            }

            dungeonStateDao.insertOrReplace(state);
            appendLog("Admin: Dungeon cooldown cleared.");
        });
    }

    private void enemyTurn(DungeonState state, Player player) {
        int damage = calculateEnemyDamage(player);

        int newPlayerHp = Math.max(0, state.getPlayerCurrentHp() - damage);
        state.setPlayerCurrentHp(newPlayerHp);
        state.setTurnNumber(state.getTurnNumber() + 1);

        if (newPlayerHp <= 0) {
            finishDefeat(state, "The Stone Golem hits for " + damage + " damage. You were defeated.");
            return;
        }

        dungeonStateDao.update(state);
        appendLog("The Stone Golem hits you for " + damage + " damage.");
    }

    private void finishVictory(DungeonState state, String message) {
        Player player = playerDao.getPlayerSync();
        if (player == null) {
            appendLog("Victory registered, but player not found.");
            return;
        }

        long now = System.currentTimeMillis();

        player.setCurrentHp(state.getPlayerCurrentHp());
        player.setCurrentMana(state.getPlayerCurrentMana());
        playerDao.updatePlayer(player);

        playerRepository.addXp(state.getRewardXp());
        playerRepository.addGold(state.getRewardGold());

        state.setStatus("VICTORY");
        state.setFinishedAt(now);
        state.setCooldownUntil(now + DUNGEON_COOLDOWN_MS);
        state.setEnemyName("");
        state.setEnemyCurrentHp(0);
        state.setEnemyMaxHp(0);
        state.setTurnNumber(1);

        dungeonStateDao.update(state);
        appendLog(message + " Reward: +" + state.getRewardXp() + " XP, +" + state.getRewardGold() + " Gold.");
    }

    private void finishDefeat(DungeonState state, String message) {
        Player player = playerDao.getPlayerSync();
        if (player != null) {
            player.setCurrentHp(1);
            player.setCurrentMana(state.getPlayerCurrentMana());
            playerDao.updatePlayer(player);
        }

        long now = System.currentTimeMillis();

        state.setStatus("DEFEAT");
        state.setFinishedAt(now);
        state.setCooldownUntil(now + DUNGEON_COOLDOWN_MS);
        state.setEnemyName("");
        state.setEnemyCurrentHp(0);
        state.setEnemyMaxHp(0);
        state.setTurnNumber(1);

        dungeonStateDao.update(state);
        appendLog(message);
    }

    private boolean isBattleReady(DungeonState state, Player player) {
        if (state == null || player == null) {
            appendLog("Dungeon state not ready.");
            return false;
        }

        if (!"IN_PROGRESS".equals(state.getStatus())) {
            appendLog("No active dungeon run.");
            return false;
        }

        return true;
    }

    private DungeonState getOrCreateState() {
        DungeonState state = dungeonStateDao.getDungeonStateSync();
        if (state == null) {
            state = new DungeonState();
            state.setId(1);
            state.setStatus("IDLE");
            dungeonStateDao.insertOrReplace(state);
        }
        return state;
    }

    private int calculateEnemyMaxHp(Player player) {
        return 80 + (player.getLevel() * 12);
    }

    private int calculateRewardXp(Player player) {
        return 60 + (player.getLevel() * 15);
    }

    private int calculateRewardGold(Player player) {
        return 25 + (player.getLevel() * 8);
    }

    private int calculatePlayerAttackDamage(Player player) {
        int strength = Math.max(1, player.getStrength());
        int dexterity = Math.max(1, player.getDexterity());
        return 10 + strength * 2 + dexterity + random.nextInt(8);
    }

    private int calculatePlayerSkillDamage(Player player) {
        String heroClass = player.getHeroClass();
        int intelligence = Math.max(1, player.getIntelligence());
        int strength = Math.max(1, player.getStrength());
        int dexterity = Math.max(1, player.getDexterity());

        if ("Mage".equals(heroClass)) {
            return 18 + intelligence * 3 + random.nextInt(10);
        } else if ("Warrior".equals(heroClass)) {
            return 16 + strength * 3 + random.nextInt(8);
        } else if ("Ranger".equals(heroClass)) {
            return 15 + dexterity * 3 + random.nextInt(9);
        }

        return 14 + intelligence * 2 + random.nextInt(8);
    }

    private int calculateEnemyDamage(Player player) {
        int base = 8 + player.getLevel() * 2;
        int mitigation = Math.max(0, player.getStrength() / 3);
        return Math.max(4, base - mitigation + random.nextInt(6));
    }

    private void appendLog(String message) {
        String current = battleLog.getValue();

        if (current == null || current.trim().isEmpty()) {
            battleLog.postValue("• " + message);
        } else {
            battleLog.postValue(current + "\n• " + message);
        }
    }

    private void clearLog() {
        battleLog.postValue("");
    }

    public void shutdown() {
        executor.shutdown();
    }
}