# pacman game
"""
SMART PAC-MAN: AI Enemy Hunter
University AI Lab Assignment - Single-file Python 3 + Pygame project.
Install: pip install pygame
Run:     python main.py
"""
import heapq
import math
import time
from dataclasses import dataclass
import pygame
# =============================================================================
# SECTION 1: CONFIGS, GRID MAPS, AND COLOR PALETTES
# =============================================================================
ROWS, COLS = 15, 15
CELL = 46
MAZE_W, MAZE_H = COLS * CELL, ROWS * CELL
SIDEBAR_W = 350
WIDTH, HEIGHT = MAZE_W + SIDEBAR_W, MAZE_H
FPS = 60
PLAYER_DELAY = 0.105
GHOST_DELAY = 0.210
MINIMAX_DEPTH = 3
# 1 = metallic wall, 0 = walkable path
MAZE_MAP = [
    "111111111111111",
    "100000100000001",
    "101110101011101",
    "101000000000101",
    "101011111110101",
    "100010000010001",
    "111010111010111",
    "100000101000001",
    "101110101011101",
    "100010000010001",
    "101011111110101",
    "101000000000101",
    "101110101011101",
    "100000100000001",
    "111111111111111",
]
PLAYER_START = (13, 1)
GHOST_START = (1, 13)
BG = (13, 16, 24)
PATH = (17, 20, 29)
GRID = (27, 31, 42)
PANEL = (18, 22, 33)
PANEL_BORDER = (45, 55, 74)
WALL = (21, 42, 69)
WALL_EDGE = (43, 102, 158)
WALL_GLOW = (31, 75, 120)
YELLOW = (255, 221, 40)
YELLOW_GLOW = (255, 235, 110)
RED = (232, 52, 72)
WHITE = (245, 248, 255)
BLUE = (30, 80, 220)
GOLD = (255, 193, 35)
GOLD_GLOW = (255, 222, 105)
GREEN = (72, 255, 151)
SEARCH = (255, 215, 60)
TEXT = (235, 239, 248)
MUTED = (150, 159, 178)
ACCENT = (117, 201, 255)
DANGER = (255, 92, 110)
SUCCESS = (86, 235, 149)
DIRECTIONS = {
    "UP": (-1, 0), "DOWN": (1, 0),
    "LEFT": (0, -1), "RIGHT": (0, 1),
}
# =============================================================================
# SECTION 2: GRID REPRESENTATION & MAZE BUILDER
# =============================================================================
class Maze:
    """Static maze plus helper methods used by movement and AI algorithms."""
    def __init__(self, source):
        self.grid = [[int(value) for value in row] for row in source]
    def is_walkable(self, row, col):
        inside = 0 <= row < ROWS and 0 <= col < COLS
        return inside and self.grid[row][col] == 0
    def neighbors(self, position):
        row, col = position
        result = []
        for dr, dc in DIRECTIONS.values():
            nr, nc = row + dr, col + dc
            if self.is_walkable(nr, nc):
                result.append((nr, nc))
        return result
    def walkable_cells(self):
        result = []
        for row in range(ROWS):
            for col in range(COLS):
                if self.is_walkable(row, col):
                    result.append((row, col))
        return result
def cell_center(position):
    row, col = position
    return col * CELL + CELL // 2, row * CELL + CELL // 2
def manhattan(a, b):
    return abs(a[0] - b[0]) + abs(a[1] - b[1])
# =============================================================================
# SECTION 3: PLAYER & COLLECTIBLE COIN CLASSES
# =============================================================================
@dataclass
class Coin:
    row: int
    col: int
    collected: bool = False
    @property
    def position(self):
        return self.row, self.col
class Player:
    """Pac-Man moves one grid cell at a time using the arrow keys."""
    def __init__(self):
        self.reset()
    def reset(self):
        self.position = PLAYER_START
        self.direction = "RIGHT"
        self.last_move = 0.0
    def move(self, maze, direction, now):
        if now - self.last_move < PLAYER_DELAY:
            return False
        dr, dc = DIRECTIONS[direction]
        row, col = self.position
        destination = (row + dr, col + dc)
        if not maze.is_walkable(*destination):
            return False
        self.position = destination
        self.direction = direction
        self.last_move = now
        return True
class CoinManager:
    """Places collectible coins on every normal walkable maze cell."""
    def __init__(self, maze):
        self.coins = []
        for row, col in maze.walkable_cells():
            if (row, col) not in (PLAYER_START, GHOST_START):
                self.coins.append(Coin(row, col))
    def reset(self):
        for coin in self.coins:
            coin.collected = False
    def collect(self, position):
        for coin in self.coins:
            if coin.position == position and not coin.collected:
                coin.collected = True
                return True
        return False
    def remaining(self):
        return sum(1 for coin in self.coins if not coin.collected)
# =============================================================================
# SECTION 4: AI ENGINE (A* ALGORITHM & MINIMAX WITH ALPHA-BETA PRUNING)
# =============================================================================
@dataclass
class AIStats:
    nodes: int = 0
    alpha: float = -math.inf
    beta: float = math.inf
    execution_ms: float = 0.0
    best_score: float = 0.0
class GhostAI:
    """
    A* supplies shortest-path knowledge.
    Minimax predicts player escape moves.
    Alpha-Beta Pruning skips branches that cannot improve the decision.
    """
    def __init__(self):
        self.reset()
    def reset(self):
        self.position = GHOST_START
        self.last_move = time.perf_counter()
        self.path = []
        self.evaluated = []
        self.pruned = []
        self.stats = AIStats()
    def a_star(self, maze, start, goal):
        """Classic A*: f(n) = g(n) + Manhattan heuristic h(n)."""
        if start == goal:
            return [start]
        frontier = [(0, start)]
        came_from = {start: None}
        cost = {start: 0}
        while frontier:
            _, current = heapq.heappop(frontier)
            if current == goal:
                break
            for nxt in maze.neighbors(current):
                new_cost = cost[current] + 1
                if nxt not in cost or new_cost < cost[nxt]:
                    cost[nxt] = new_cost
                    priority = new_cost + manhattan(nxt, goal)
                    heapq.heappush(frontier, (priority, nxt))
                    came_from[nxt] = current
        if goal not in came_from:
            return []
        path = []
        current = goal
        while current is not None:
            path.append(current)
            current = came_from[current]
        path.reverse()
        return path
    def evaluate_state(self, maze, ghost_pos, player_pos):
        """Higher is better for the ghost."""
        path = self.a_star(maze, ghost_pos, player_pos)
        distance = len(path) - 1 if path else 999
        escape_options = len(maze.neighbors(player_pos))
        distance_score = 130 - distance * 12
        trap_bonus = (4 - escape_options) * 9
        return distance_score + trap_bonus
    def minimax(self, maze, ghost_pos, player_pos, depth, maximizing, alpha, beta):
        self.stats.nodes += 1
        if ghost_pos not in self.evaluated:
            self.evaluated.append(ghost_pos)
        if ghost_pos == player_pos:
            return 10000 + depth * 100
        if depth == 0:
            return self.evaluate_state(maze, ghost_pos, player_pos)
        if maximizing:
            # MAX = Ghost. It chooses the highest-scoring chase position.
            best = -math.inf
            moves = maze.neighbors(ghost_pos)
            for index, nxt in enumerate(moves):
                score = self.minimax(
                    maze, nxt, player_pos, depth - 1, False, alpha, beta
                )
                best = max(best, score)
                alpha = max(alpha, best)
                self.stats.alpha = max(self.stats.alpha, alpha)
                # beta <= alpha means remaining siblings cannot change the choice.
                if beta <= alpha:
                    self.pruned.extend(moves[index + 1:])
                    break
            return best
        # MIN = Pac-Man. It selects the simulated move best for escaping.
        best = math.inf
        moves = maze.neighbors(player_pos)
        if not moves:
            return self.evaluate_state(maze, ghost_pos, player_pos)
        for index, nxt in enumerate(moves):
            score = self.minimax(
                maze, ghost_pos, nxt, depth - 1, True, alpha, beta
            )
            best = min(best, score)
            beta = min(beta, best)
            self.stats.beta = min(self.stats.beta, beta)
            if beta <= alpha:
                self.pruned.extend(moves[index + 1:])
                break
        return best
    def choose_move(self, maze, player_pos):
        start = time.perf_counter()
        self.stats = AIStats()
        self.evaluated = []
        self.pruned = []
        legal_moves = maze.neighbors(self.position)
        if not legal_moves:
            self.path = []
            return self.position
        best_move = legal_moves[0]
        best_score = -math.inf
        alpha = -math.inf
        beta = math.inf
        for move in legal_moves:
            if move not in self.evaluated:
                self.evaluated.append(move)
            score = self.minimax(
                maze, move, player_pos,
                MINIMAX_DEPTH - 1, False, alpha, beta
            )
            if score > best_score:
                best_score = score
                best_move = move
            alpha = max(alpha, best_score)
            self.stats.alpha = max(self.stats.alpha, alpha)
        # The complete A* route chosen by the tactical decision is visualized.
        route = self.a_star(maze, best_move, player_pos)
        self.path = [self.position] + route if route else []
        self.stats.best_score = best_score
        self.stats.execution_ms = (time.perf_counter() - start) * 1000.0
        print(
            f"[AI LOG] Best Move chosen: {best_move} | "
            f"Alpha: {self.bound(self.stats.alpha)} | "
            f"Beta: {self.bound(self.stats.beta)} | "
            f"Nodes Evaluated: {self.stats.nodes} "
            f"in {self.stats.execution_ms / 1000.0:.4f}s"
        )
        return best_move
    def update(self, maze, player_pos, now):
        if now - self.last_move < GHOST_DELAY:
            return False
        self.position = self.choose_move(maze, player_pos)
        self.last_move = now
        return True
    @staticmethod
    def bound(value):
        if value == math.inf:
            return "+INF"
        if value == -math.inf:
            return "-INF"
        return f"{value:.1f}"
# =============================================================================
# SECTION 5: PYGAME GRAPHICS ENGINE & HUD RENDERING (DRAW FUNCTIONS)
# =============================================================================
class Renderer:
    """Responsible only for drawing. Game logic stays outside this class."""
    def __init__(self, screen):
        self.screen = screen
        self.title_font = pygame.font.SysFont("consolas", 24, bold=True)
        self.heading_font = pygame.font.SysFont("consolas", 18, bold=True)
        self.font = pygame.font.SysFont("consolas", 15)
        self.small = pygame.font.SysFont("consolas", 13)
    def draw(self, maze, player, ghost, coins, score, seconds, status):
        self.screen.fill(BG)
        self.draw_maze(maze)
        self.draw_search_overlay(ghost, seconds)
        self.draw_coins(coins, seconds)
        self.draw_astar_path(ghost)
        self.draw_pacman(player, seconds)
        self.draw_ghost(ghost, seconds)
        self.draw_hud(ghost, score, coins.remaining(), seconds, status)
    def draw_maze(self, maze):
        for row in range(ROWS):
            for col in range(COLS):
                rect = pygame.Rect(col * CELL, row * CELL, CELL, CELL)
                if maze.grid[row][col] == 1:
                    pygame.draw.rect(self.screen, WALL_GLOW, rect, border_radius=5)
                    inner = rect.inflate(-4, -4)
                    pygame.draw.rect(self.screen, WALL, inner, border_radius=5)
                    pygame.draw.rect(
                        self.screen, WALL_EDGE, inner, width=2, border_radius=5
                    )
                else:
                    pygame.draw.rect(self.screen, PATH, rect)
                    pygame.draw.rect(self.screen, GRID, rect, width=1)
    def draw_coins(self, coin_manager, seconds):
        pulse = 1.0 + math.sin(seconds * 6.0) * 0.18
        for coin in coin_manager.coins:
            if coin.collected:
                continue
            center = cell_center(coin.position)
            glow = pygame.Surface((18, 18), pygame.SRCALPHA)
            pygame.draw.circle(
                glow, (*GOLD_GLOW, 55), (9, 9), max(5, int(7 * pulse))
            )
            self.screen.blit(glow, (center[0] - 9, center[1] - 9))
            pygame.draw.circle(self.screen, GOLD, center, 4)
    def draw_pacman(self, player, seconds):
        x, y = cell_center(player.position)
        radius = CELL // 2 - 6
        pygame.draw.circle(self.screen, YELLOW_GLOW, (x, y), radius + 3)
        pygame.draw.circle(self.screen, YELLOW, (x, y), radius)
        mouth = math.radians(18 + abs(math.sin(seconds * 8.0)) * 27)
        facing = {"RIGHT": 0, "DOWN": 90, "LEFT": 180, "UP": 270}
        angle = math.radians(facing[player.direction])

        p1 = (x + math.cos(angle - mouth) * radius,
              y + math.sin(angle - mouth) * radius)
        p2 = (x + math.cos(angle + mouth) * radius,
              y + math.sin(angle + mouth) * radius)
        pygame.draw.polygon(self.screen, PATH, [(x, y), p1, p2])

    def draw_ghost(self, ghost, seconds):
        x, y = cell_center(ghost.position)
        body_w = CELL - 10
        radius = body_w // 2

        glow = pygame.Surface((CELL, CELL), pygame.SRCALPHA)
        glow_alpha = int(45 + abs(math.sin(seconds * 4.0)) * 40)
        pygame.draw.circle(
            glow, (*RED, glow_alpha), (CELL // 2, CELL // 2), CELL // 2 - 2
        )
        self.screen.blit(glow, (x - CELL // 2, y - CELL // 2))

        pygame.draw.circle(self.screen, RED, (x, y - 4), radius)
        pygame.draw.rect(
            self.screen, RED, (x - radius, y - 4, body_w, radius + 8)
        )
        foot_radius = max(3, body_w // 8)
        for index in range(4):
            foot_x = x - radius + foot_radius + index * foot_radius * 2
            pygame.draw.circle(self.screen, RED, (foot_x, y + radius), foot_radius)

        for offset in (-7, 7):
            eye = (x + offset, y - 5)
            pygame.draw.circle(self.screen, WHITE, eye, 5)
            pygame.draw.circle(self.screen, BLUE, (eye[0] + 1, eye[1] + 1), 2)

    def draw_astar_path(self, ghost):
        if len(ghost.path) < 2:
            return

        points = [cell_center(position) for position in ghost.path]
        overlay = pygame.Surface((MAZE_W, MAZE_H), pygame.SRCALPHA)
        pygame.draw.lines(overlay, (*GREEN, 170), False, points, width=4)

        for point in points:
            pygame.draw.circle(overlay, (*GREEN, 105), point, 6)

        # Small arrows communicate path direction to an examiner.
        for index in range(len(points) - 1):
            x1, y1 = points[index]
            x2, y2 = points[index + 1]
            middle = ((x1 + x2) // 2, (y1 + y2) // 2)
            pygame.draw.circle(overlay, (*GREEN, 210), middle, 3)

        self.screen.blit(overlay, (0, 0))

    def draw_search_overlay(self, ghost, seconds):
        """Yellow = visited Minimax nodes, red = Alpha-Beta pruned siblings."""
        overlay = pygame.Surface((MAZE_W, MAZE_H), pygame.SRCALPHA)
        pulse = 0.55 + abs(math.sin(seconds * 8.0)) * 0.45
        yellow_alpha = int(80 * pulse)

        evaluated = list(dict.fromkeys(ghost.evaluated))[:28]
        pruned = list(dict.fromkeys(ghost.pruned))[:20]
        ghost_center = cell_center(ghost.position)

        for cell in evaluated:
            center = cell_center(cell)
            pygame.draw.circle(
                overlay, (*SEARCH, yellow_alpha), center, 9
            )
            pygame.draw.line(
                overlay, (*SEARCH, int(yellow_alpha * 0.45)),
                ghost_center, center, width=1
            )

        for row, col in pruned:
            rect = pygame.Rect(
                col * CELL + 10, row * CELL + 10, CELL - 20, CELL - 20
            )
            pygame.draw.rect(overlay, (*RED, 55), rect, border_radius=5)

        self.screen.blit(overlay, (0, 0))

    def stat(self, x, y, label, value, value_color=TEXT):
        self.screen.blit(self.small.render(label, True, MUTED), (x, y))
        self.screen.blit(
            self.heading_font.render(value, True, value_color), (x, y + 18)
        )

    def draw_hud(self, ghost, score, remaining, seconds, status):
        panel_rect = pygame.Rect(MAZE_W, 0, SIDEBAR_W, HEIGHT)
        pygame.draw.rect(self.screen, PANEL, panel_rect)
        pygame.draw.line(
            self.screen, PANEL_BORDER, (MAZE_W, 0), (MAZE_W, HEIGHT), width=2
        )

        x, y = MAZE_W + 24, 20
        self.screen.blit(
            self.title_font.render("SMART PAC-MAN", True, TEXT), (x, y)
        )
        self.screen.blit(
            self.small.render("AI ENEMY HUNTER / LAB HUD", True, ACCENT),
            (x, y + 32),
        )
        y += 72

        pygame.draw.circle(self.screen, SUCCESS, (x + 7, y + 8), 6)
        self.screen.blit(
            self.font.render("Alpha-Beta Engine Active", True, SUCCESS),
            (x + 20, y),
        )
        y += 38

        stats = [
            ("SCORE", str(score)),
            ("REMAINING COINS", str(remaining)),
            ("GAME TIMER", f"{seconds:.1f} s"),
            ("NODES EVALUATED", str(ghost.stats.nodes)),
            ("ALPHA / BETA",
             f"{ghost.bound(ghost.stats.alpha)} / {ghost.bound(ghost.stats.beta)}"),
            ("EXECUTION TIME", f"{ghost.stats.execution_ms:.3f} ms"),
            ("BEST SCORE", f"{ghost.stats.best_score:.1f}"),
        ]

        for label, value in stats:
            self.stat(x, y, label, value)
            y += 45

        status_color = TEXT
        if status == "GAME OVER - CAUGHT":
            status_color = DANGER
        elif status == "VICTORY":
            status_color = SUCCESS

        self.stat(x, y, "GAME STATUS", status, status_color)
        y += 55

        self.screen.blit(self.small.render("CONTROLS", True, MUTED), (x, y))
        y += 22

        lines = [
            "Arrow Keys : Move Pac-Man",
            "[R]        : Restart Game",
            "[ESC]      : Exit",
        ]
        for line in lines:
            self.screen.blit(self.small.render(line, True, TEXT), (x, y))
            y += 20

        y += 8
        self.screen.blit(
            self.small.render("Green  = A* chosen path", True, GREEN), (x, y)
        )
        y += 19
        self.screen.blit(
            self.small.render("Yellow = Minimax evaluated", True, SEARCH), (x, y)
        )
        y += 19
        self.screen.blit(
            self.small.render("Red    = Alpha-Beta pruned", True, RED), (x, y)
        )
        y += 27
        self.screen.blit(
            self.font.render("Press [R] to Restart", True, ACCENT), (x, y)
        )


# =============================================================================
# SECTION 6: MAIN EVENT LOOP & GAME CONTROLLER
# =============================================================================

class Game:
    """Coordinates player input, AI updates, scoring, timing, and rendering."""

    def __init__(self):
        pygame.init()
        pygame.display.set_caption("Smart Pac-Man - AI Enemy Hunter")
        self.screen = pygame.display.set_mode((WIDTH, HEIGHT))
        self.clock = pygame.time.Clock()

        self.maze = Maze(MAZE_MAP)
        self.player = Player()
        self.ghost = GhostAI()
        self.coins = CoinManager(self.maze)
        self.renderer = Renderer(self.screen)

        self.score = 0
        self.status = "PLAYING"
        self.started_at = time.perf_counter()
        self.finished_at = None

    def restart(self):
        self.player.reset()
        self.ghost.reset()
        self.coins.reset()
        self.score = 0
        self.status = "PLAYING"
        self.started_at = time.perf_counter()
        self.finished_at = None
        print("[GAME] Restarted.")

    def elapsed(self):
        end_time = self.finished_at
        if end_time is None:
            end_time = time.perf_counter()
        return end_time - self.started_at

    def check_result(self):
        if self.player.position == self.ghost.position:
            self.status = "GAME OVER - CAUGHT"
            self.finished_at = time.perf_counter()
            print(f"[GAME] Pac-Man caught. Final Score: {self.score}")
            return

        if self.coins.remaining() == 0:
            self.status = "VICTORY"
            self.finished_at = time.perf_counter()
            print(f"[GAME] Victory! Final Score: {self.score}")

    def process_player(self):
        if self.status != "PLAYING":
            return

        keys = pygame.key.get_pressed()
        direction = None

        if keys[pygame.K_UP]:
            direction = "UP"
        elif keys[pygame.K_DOWN]:
            direction = "DOWN"
        elif keys[pygame.K_LEFT]:
            direction = "LEFT"
        elif keys[pygame.K_RIGHT]:
            direction = "RIGHT"

        if direction is None:
            return

        moved = self.player.move(
            self.maze, direction, time.perf_counter()
        )

        if moved:
            if self.coins.collect(self.player.position):
                self.score += 10
            self.check_result()

    def update_ai(self):
        if self.status != "PLAYING":
            return

        moved = self.ghost.update(
            self.maze,
            self.player.position,
            time.perf_counter(),
        )

        if moved:
            self.check_result()

    def run(self):
        running = True

        while running:
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    running = False

                elif event.type == pygame.KEYDOWN:
                    if event.key == pygame.K_ESCAPE:
                        running = False
                    elif event.key == pygame.K_r:
                        self.restart()

            self.process_player()
            self.update_ai()

            self.renderer.draw(
                self.maze,
                self.player,
                self.ghost,
                self.coins,
                self.score,
                self.elapsed(),
                self.status,
            )

            pygame.display.flip()
            self.clock.tick(FPS)

        pygame.quit()


def main():
    Game().run()


if __name__ == "__main__":
    main()
