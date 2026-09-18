import re
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

COMMON_SKILLS = {
    "python", "java", "javascript", "typescript", "react", "angular", "vue",
    "spring", "spring boot", "fastapi", "flask", "django", "sql", "mysql",
    "postgresql", "mongodb", "redis", "docker", "kubernetes", "aws", "azure",
    "git", "github", "rest api", "machine learning", "deep learning",
    "nlp", "html", "css", "node.js", "express", "pandas", "numpy",
    "scikit-learn", "tensorflow", "pytorch", "linux", "microservices"
}

ALIASES = {
    "js": "javascript",
    "javascript": "javascript",
    "react.js": "react",
    "reactjs": "react",
    "postgres": "postgresql",
    "postgres db": "postgresql",
    "springboot": "spring boot",
    "rest": "rest api",
    "ml": "machine learning",
    "dl": "deep learning",
    "sklearn": "scikit-learn"
}


def normalize(text: str) -> str:
    text = (text or "").lower()
    for alias, canonical in ALIASES.items():
        text = re.sub(r"\b" + re.escape(alias) + r"\b", canonical, text)
    return re.sub(r"\s+", " ", text).strip()


def skill_set(text: str):
    normalized = normalize(text)
    return {skill for skill in COMMON_SKILLS if skill in normalized}


def cosine_score(resume: str, job: str) -> float:
    if not resume.strip() or not job.strip():
        return 0.0
    vectorizer = TfidfVectorizer(stop_words="english", ngram_range=(1, 2))
    matrix = vectorizer.fit_transform([normalize(resume), normalize(job)])
    return float(cosine_similarity(matrix[0:1], matrix[1:2])[0][0] * 100)


def match_resume(resume_text: str, job_description: str):
    resume_skills = skill_set(resume_text)
    job_skills = skill_set(job_description)

    matched = sorted(resume_skills & job_skills)
    missing = sorted(job_skills - resume_skills)

    if job_skills:
        skill_score = len(matched) / len(job_skills) * 100
    else:
        skill_score = 0.0

    semantic_score = cosine_score(resume_text, job_description)
    tfidf_score = semantic_score

    overall = (0.40 * semantic_score) + (0.30 * tfidf_score) + (0.30 * skill_score)

    return {
        "semantic_score": round(semantic_score, 2),
        "tfidf_score": round(tfidf_score, 2),
        "skill_score": round(skill_score, 2),
        "overall_score": round(overall, 2),
        "matched_skills": matched,
        "missing_skills": missing,
        "resume_text": resume_text
    }
