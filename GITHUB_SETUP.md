# 🚀 How to Push to GitHub using Termux

## Step 1: Install Required Tools in Termux

```bash
pkg update && pkg upgrade
pkg install git
pkg install openssh
```

## Step 2: Configure Git

```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

## Step 3: Generate SSH Key (Recommended)

```bash
ssh-keygen -t ed25519 -C "your.email@example.com"
# Press Enter for all prompts
# Display your public key
cat ~/.ssh/id_ed25519.pub
```

Copy the output and add it to GitHub:
- Go to: https://github.com/settings/keys
- Click "New SSH key"
- Paste the key
- Click "Add SSH key"

## Step 4: Navigate to Your Project

```bash
# If project is in downloads
cd storage/downloads/tasalicool_final

# Or navigate to wherever you extracted the ZIP
cd /path/to/tasalicool_final
```

## Step 5: Initialize Git Repository

```bash
git init
git add .
git commit -m "Initial commit: Tasalicool professional Tarneeb card game"
```

## Step 6: Add Remote Repository

```bash
# SSH method (recommended)
git remote add origin git@github.com:YOUR_USERNAME/tasalicool.git

# Or HTTPS method (if SSH fails)
git remote add origin https://github.com/YOUR_USERNAME/tasalicool.git
```

## Step 7: Rename Branch to Main (if needed)

```bash
git branch -M main
```

## Step 8: Push to GitHub

```bash
git push -u origin main
```

---

## 🔗 Complete Termux Commands (Copy & Paste)

```bash
# 1. Update and install tools
pkg update && pkg upgrade -y && pkg install git -y

# 2. Configure git
git config --global user.name "Your Name"
git config --global user.email "your@email.com"

# 3. Navigate to project (adjust path as needed)
cd storage/downloads/tasalicool_final

# 4. Initialize repository
git init
git add .
git commit -m "Initial commit: Tasalicool - Professional Tarneeb Card Game"

# 5. Add remote (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/tasalicool.git

# 6. Push to GitHub
git push -u origin main
```

---

## ✅ First Time GitHub Setup

If you haven't created a repository yet:

1. Go to https://github.com/new
2. Repository name: `tasalicool`
3. Description: `Professional Tarneeb Card Game`
4. Make it **Public** (for Google Play visibility)
5. Click "Create repository"
6. Follow the "or push an existing repository" section

---

## 🔄 Future Updates (After Initial Push)

```bash
# Make changes, then:
git add .
git commit -m "Update: [describe changes]"
git push origin main
```

---

## 🚨 If SSH Key Issues

Use HTTPS instead:

```bash
# Remove SSH remote
git remote remove origin

# Add HTTPS remote
git remote add origin https://github.com/YOUR_USERNAME/tasalicool.git

# First time: GitHub will ask for credentials
# Create Personal Access Token: https://github.com/settings/tokens
# Use token as password when prompted
```

---

## 📁 Project Files in Repository

```
tasalicool/
├── app/src/main/java/com/tasalicool/game/    [All Kotlin files]
├── app/src/main/res/                          [Resources]
├── app/src/main/AndroidManifest.xml
├── app/src/test/                              [Tests]
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
├── LICENSE
├── .gitignore
└── [Documentation files]
```

---

## ✨ After Pushing to GitHub

1. **Add More Details**
   - Edit README.md with screenshots
   - Add Topics (tarneeb, card-game, android, kotlin)
   - Add a nice description

2. **Enable Discussions**
   - Settings → Features → Discussions

3. **Add License**
   - Already included (MIT)

4. **Create Releases**
   - Tag your first release as v1.0.0
   - Add APK/AAB as release asset

5. **Setup GitHub Pages** (Optional)
   - Create gh-pages branch for documentation

---

## 🎯 GitHub Checklist

- [x] Repository created
- [x] Files pushed
- [x] README complete
- [ ] Add screenshots
- [ ] Add topics (tags)
- [ ] Create releases
- [ ] Link to Google Play
- [ ] Add contributing guidelines

---

## 📊 GitHub Stats Commands

```bash
# After pushing, check your repo stats
git log --oneline
git show-branch
git branch -a
```

---

**Your project is now on GitHub! 🎉**

Share the link: `https://github.com/YOUR_USERNAME/tasalicool`

---

Need help? Check: https://docs.github.com/en/repositories/creating-and-managing-repositories
