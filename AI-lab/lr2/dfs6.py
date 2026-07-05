grid = [
    [0, 0, 1, 0, 0, 0],
    [1, 0, 1, 0, 1, 0],
    [0, 0, 0, 0, 1, 0],
    [0, 1, 1, 0, 0, 0],
    [0, 0, 0, 1, 1, 0],
    [1, 1, 0, 0, 0, 0]
]

n = 6
vis = [[0] * n for _ in range(n)]
path = []
moves = [(1, 0, "Down"), (0, 1, "Right"), (-1, 0, "Up"), (0, -1, "Left")]

def dfs(x, y):
    if (x, y) == (n - 1, n - 1):
        return True
    vis[x][y] = 1
    for dx, dy, d in moves:
        nx, ny = x + dx, y + dy
        if 0 <= nx < n and 0 <= ny < n and grid[nx][ny] == 0 and not vis[nx][ny]:
            path.append((d, nx, ny))
            if dfs(nx, ny): 
              return True
            path.pop()
    return False

if grid[0][0] == 0 and dfs(0, 0):
    print("Goal found")
    print("Number of moves required =", len(path))
    for d, x, y in path:
        print(f"Moving {d} ({x}, {y})")
else:
    print("Goal not found")
