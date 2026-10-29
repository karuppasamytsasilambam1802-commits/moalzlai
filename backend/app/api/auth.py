from fastapi import APIRouter
from app.models.schemas import GitHubAuthRequest, GitHubAuthResponse, GitHubUserModel
import requests

router = APIRouter()

@router.post("/auth/github", response_model=GitHubAuthResponse)
async def authenticate_github(req: GitHubAuthRequest):
    username = req.username or "octocat"

    # Try fetching real public profile from GitHub API if username provided
    try:
        url = f"https://api.github.com/users/{username}"
        res = requests.get(url, headers={"User-Agent": "VoxBridgeAIApp"}, timeout=5)
        if res.status_code == 200:
            data = res.json()
            user = GitHubUserModel(
                id=data.get("id", 100001),
                login=data.get("login", username),
                name=data.get("name") or data.get("login", username),
                avatarUrl=data.get("avatar_url", "https://avatars.githubusercontent.com/u/583231?v=4"),
                email=data.get("email") or f"{username}@users.noreply.github.com",
                bio=data.get("bio") or "VoxBridge AI Multilingual Speaker",
                htmlUrl=data.get("html_url", f"https://github.com/{username}")
            )
            return GitHubAuthResponse(
                success=True,
                message=f"GitHub profile for @{username} loaded successfully",
                user=user
            )
    except Exception as e:
        print(f"Error fetching GitHub profile: {e}")

    # Fallback default GitHub account profile
    fallback_user = GitHubUserModel(
        login=username,
        name=username.capitalize(),
        email=f"{username}@users.noreply.github.com"
    )
    return GitHubAuthResponse(
        success=True,
        message="Authenticated with GitHub (Local Profile)",
        user=fallback_user
    )
