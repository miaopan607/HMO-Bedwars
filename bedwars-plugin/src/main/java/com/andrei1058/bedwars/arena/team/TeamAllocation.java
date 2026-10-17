package com.andrei1058.bedwars.arena.team;

/** Integer-only allocation planning; fixed members are never moved. */
final class TeamAllocation {
    private TeamAllocation() {}

    static int[] targetSizes(int[] fixedSizes, int remainingPlayers, int maxInTeam) {
        if (fixedSizes == null || remainingPlayers < 0 || maxInTeam < 1) {
            throw new IllegalArgumentException("Invalid team allocation input");
        }

        long total = remainingPlayers;
        long fixedTotal = 0;
        int occupied = 0;
        for (int size : fixedSizes) {
            if (size < 0 || size > maxInTeam) {
                throw new IllegalArgumentException("Invalid fixed team size");
            }
            total += size;
            fixedTotal += size;
            if (size > 0) occupied++;
        }
        if (total > (long) fixedSizes.length * maxInTeam) {
            throw new IllegalArgumentException("Insufficient team capacity");
        }
        if (total > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Too many players");
        }

        int[] best = new int[fixedSizes.length];
        System.arraycopy(fixedSizes, 0, best, 0, fixedSizes.length);
        if (remainingPlayers == 0) return best;

        int totalPlayers = (int) total;
        int maximum = (int) Math.min(fixedSizes.length, Math.min((long) occupied + remainingPlayers, total));
        int minimum = Math.max(occupied, Math.min(2, maximum));
        int bestImbalance = Integer.MAX_VALUE;
        int bestSingles = Integer.MAX_VALUE;
        int bestCount = -1;

        for (int count = minimum; count <= maximum; count++) {
            if ((long) count * maxInTeam < total) continue;
            if (fixedTotal + count - occupied > total) continue;

            int[] candidate = waterLevelTargets(fixedSizes, totalPlayers, count, maxInTeam);
            if (candidate == null) continue;

            int imbalance = Math.max(0, spread(candidate) - 1);
            int singles = singles(candidate);
            if (imbalance < bestImbalance
                    || imbalance == bestImbalance && singles < bestSingles
                    || imbalance == bestImbalance && singles == bestSingles && count > bestCount) {
                System.arraycopy(candidate, 0, best, 0, best.length);
                bestImbalance = imbalance;
                bestSingles = singles;
                bestCount = count;
            }
        }

        if (bestCount < 0) throw new IllegalStateException("No feasible team allocation");
        return best;
    }

    private static int[] waterLevelTargets(int[] fixedSizes, int total, int activeCount, int maxInTeam) {
        int[] active = activeTeams(fixedSizes, activeCount);
        int low = 1;
        int high = maxInTeam;
        while (low < high) {
            int level = low + (high - low + 1) / 2;
            if (requiredAtLevel(fixedSizes, active, level) <= total) {
                low = level;
            } else {
                high = level - 1;
            }
        }

        int level = low;
        int[] target = new int[fixedSizes.length];
        long used = 0;
        for (int team : active) {
            target[team] = Math.max(fixedSizes[team], level);
            used += target[team];
        }

        int remaining = total - (int) used;
        for (int team : active) {
            if (remaining == 0) break;
            if (target[team] == level) {
                target[team]++;
                remaining--;
            }
        }
        return remaining == 0 ? target : null;
    }

    private static int[] activeTeams(int[] fixedSizes, int activeCount) {
        int[] active = new int[activeCount];
        int index = 0;
        for (int i = 0; i < fixedSizes.length && index < activeCount; i++) {
            if (fixedSizes[i] > 0) active[index++] = i;
        }
        for (int i = 0; i < fixedSizes.length && index < activeCount; i++) {
            if (fixedSizes[i] == 0) active[index++] = i;
        }
        if (index != activeCount) throw new IllegalStateException("Invalid active team count");
        return active;
    }

    private static long requiredAtLevel(int[] fixedSizes, int[] active, int level) {
        long required = 0;
        for (int team : active) {
            required += Math.max(fixedSizes[team], level);
        }
        return required;
    }

    private static int spread(int[] sizes) {
        int minimum = Integer.MAX_VALUE;
        int maximum = 0;
        for (int size : sizes) {
            if (size == 0) continue;
            minimum = Math.min(minimum, size);
            maximum = Math.max(maximum, size);
        }
        return maximum - minimum;
    }

    private static int singles(int[] sizes) {
        int result = 0;
        for (int size : sizes) {
            if (size == 1) result++;
        }
        return result;
    }
}
