# Kiro 到 Codex 迁移说明

原始 `.kiro/` 目录保持不变，因此该工作区仍可继续在 Kiro 中使用。

## 目录映射

- `.kiro/steering/01.基础知识.md` 对应项目根目录的 `AGENTS.md`，并在当前项目中全局生效。
- `.codex/skills/` 与 `.kiro/skills/` 完全镜像：每个 Skill 均保留相同的目录名称、层级结构和 `SKILL.md` 内容。
- 所有 Skills 均为手动触发。需要 Codex 使用某个 Skill 时，请明确指定 Skill 名称或目录名。

## 自动同步

- Kiro 中的修改由 `.kiro/hooks/sync-kiro-to-codex.kiro.hook` 负责同步。
- Codex 中的修改由 `.codex/hooks.json` 处理；支持的文件编辑工具执行完毕后，它会运行 `.codex/hooks/sync-codex-to-kiro.sh`。
- Codex Hook 会将 `.codex/skills/` 镜像到 `.kiro/skills/`，同步新增、修改和删除，并忽略 `.DS_Store` 文件。

请避免同时在 Kiro 和 Codex 中编辑同一个 Skill；发生冲突时，以最后一次触发的同步结果为准。
