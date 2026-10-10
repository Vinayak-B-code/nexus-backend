package com.nexus.config;

import com.nexus.entity.*;
import com.nexus.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectUpdateRepository projectUpdateRepository;
    private final BuildCircleRepository circleRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final CircleMilestoneRepository circleMilestoneRepository;
    private final SkillBountyRepository bountyRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           SkillRepository skillRepository,
                           UserSkillRepository userSkillRepository,
                           ProjectRepository projectRepository,
                           ProjectMemberRepository projectMemberRepository,
                           ProjectUpdateRepository projectUpdateRepository,
                           BuildCircleRepository circleRepository,
                           CircleMemberRepository circleMemberRepository,
                           CircleMilestoneRepository circleMilestoneRepository,
                           SkillBountyRepository bountyRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectUpdateRepository = projectUpdateRepository;
        this.circleRepository = circleRepository;
        this.circleMemberRepository = circleMemberRepository;
        this.circleMilestoneRepository = circleMilestoneRepository;
        this.bountyRepository = bountyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            seedSkills();
            seedUsersAndContent();
        } catch (Exception e) {
            System.err.println("Database seeding skipped or postponed: " + e.getMessage());
        }
    }

    private void seedSkills() {
        if (skillRepository.count() > 0) return;

        List<Skill> skills = List.of(
            // Frontend
            new Skill("React", "Frontend"),
            new Skill("TypeScript", "Frontend"),
            new Skill("Next.js", "Frontend"),
            new Skill("Vue.js", "Frontend"),
            new Skill("HTML5/CSS3", "Frontend"),
            new Skill("Tailwind CSS", "Frontend"),

            // Backend
            new Skill("Spring Boot", "Backend"),
            new Skill("Java", "Backend"),
            new Skill("Node.js", "Backend"),
            new Skill("Python", "Backend"),
            new Skill("MySQL", "Backend"),
            new Skill("PostgreSQL", "Backend"),
            new Skill("Go", "Backend"),

            // Data and AI
            new Skill("PyTorch", "Data and AI"),
            new Skill("Machine Learning", "Data and AI"),
            new Skill("TensorFlow", "Data and AI"),
            new Skill("Computer Vision", "Data and AI"),
            new Skill("LLMs & RAG", "Data and AI"),

            // Design
            new Skill("UI/UX Design", "Design"),
            new Skill("Figma", "Design"),
            new Skill("Design Systems", "Design"),

            // DevOps
            new Skill("Docker", "DevOps"),
            new Skill("Kubernetes", "DevOps"),
            new Skill("AWS", "DevOps"),
            new Skill("CI/CD", "DevOps"),
            new Skill("Linux", "DevOps"),

            // Mobile
            new Skill("Flutter", "Mobile"),
            new Skill("React Native", "Mobile"),
            new Skill("Kotlin", "Mobile"),
            new Skill("Swift", "Mobile")
        );
        skillRepository.saveAll(skills);
    }

    private void seedUsersAndContent() {
        if (userRepository.count() > 0) return;

        // User 1: Aarav Sharma (Backend lead, Admin)
        User aarav = new User(
            "Aarav Sharma",
            "aarav@nexus.dev",
            passwordEncoder.encode("password123"),
            "IIT Bombay",
            "Computer Science & Engineering",
            "Year 3",
            "Passionate about distributed systems, microservices, and clean architecture. Looking for frontend collaborators!"
        );
        aarav.setRole("ROLE_ADMIN");
        aarav.setAvatarColor("#625BEE");
        aarav.setGithubUrl("https://github.com/aaravsharma");
        aarav.setLinkedinUrl("https://linkedin.com/in/aaravsharma");
        aarav = userRepository.save(aarav);

        // User 2: Rohan Deshmukh (Frontend & UI design)
        User rohan = new User(
            "Rohan Deshmukh",
            "rohan@nexus.dev",
            passwordEncoder.encode("password123"),
            "COEP Technological University, Pune",
            "Information Technology",
            "Year 2",
            "Obsessed with fluid interactions, design tokens, and React ecosystems. Need solid backend architects to turn prototypes into real apps."
        );
        rohan.setAvatarColor("#0D9488");
        rohan.setGithubUrl("https://github.com/rohandeshmukh");
        rohan = userRepository.save(rohan);

        // User 3: Ananya Patel (AI/ML)
        User ananya = new User(
            "Ananya Patel",
            "ananya@nexus.dev",
            passwordEncoder.encode("password123"),
            "BITS Pilani",
            "Computer Science",
            "Year 4",
            "Building multimodal models and RAG pipelines. Want to collaborate with web developers who can give my models beautiful user interfaces."
        );
        ananya.setAvatarColor("#E11D48");
        ananya.setGithubUrl("https://github.com/ananyapatel");
        ananya = userRepository.save(ananya);

        // User 4: Priya Kulkarni (Mobile & Systems)
        User priya = new User(
            "Priya Kulkarni",
            "priya@nexus.dev",
            passwordEncoder.encode("password123"),
            "NIT Trichy",
            "Electronics & Communication Engineering",
            "Year 3",
            "Cross-platform mobile developer with a heart for open-source developer tooling and developer experience."
        );
        priya.setAvatarColor("#D97706");
        priya = userRepository.save(priya);

        // Seed skills for Aarav
        addSkill(aarav, "Spring Boot", "Backend", "PROFICIENT", "ADVANCED");
        addSkill(aarav, "Java", "Backend", "PROFICIENT", "ADVANCED");
        addSkill(aarav, "MySQL", "Backend", "PROFICIENT", "INTERMEDIATE");
        addSkill(aarav, "Docker", "DevOps", "PROFICIENT", "INTERMEDIATE");
        addSkill(aarav, "Docker", "DevOps", "CAN_TEACH", "ADVANCED");
        addSkill(aarav, "React", "Frontend", "WANTS_TO_LEARN", "BEGINNER");

        // Seed skills for Rohan
        addSkill(rohan, "React", "Frontend", "PROFICIENT", "ADVANCED");
        addSkill(rohan, "TypeScript", "Frontend", "PROFICIENT", "ADVANCED");
        addSkill(rohan, "Tailwind CSS", "Frontend", "PROFICIENT", "ADVANCED");
        addSkill(rohan, "UI/UX Design", "Design", "PROFICIENT", "ADVANCED");
        addSkill(rohan, "Figma", "Design", "CAN_TEACH", "ADVANCED");
        addSkill(rohan, "Spring Boot", "Backend", "WANTS_TO_LEARN", "BEGINNER");

        // Seed skills for Ananya
        addSkill(ananya, "Python", "Backend", "PROFICIENT", "ADVANCED");
        addSkill(ananya, "PyTorch", "Data and AI", "PROFICIENT", "ADVANCED");
        addSkill(ananya, "Machine Learning", "Data and AI", "PROFICIENT", "ADVANCED");
        addSkill(ananya, "LLMs & RAG", "Data and AI", "CAN_TEACH", "ADVANCED");
        addSkill(ananya, "Next.js", "Frontend", "WANTS_TO_LEARN", "BEGINNER");

        // Seed skills for Priya
        addSkill(priya, "Flutter", "Mobile", "PROFICIENT", "ADVANCED");
        addSkill(priya, "Kotlin", "Mobile", "PROFICIENT", "INTERMEDIATE");
        addSkill(priya, "Go", "Backend", "PROFICIENT", "INTERMEDIATE");

        // Seed Projects
        Project p1 = new Project();
        p1.setTitle("EchoFlow - Audio Workspace");
        p1.setShortDescription("Collaborative stem mixer and real-time audio annotation workbench for student creators.");
        p1.setDetailedDescription("EchoFlow solves asynchronous music and podcast critique by allowing students to leave timestamped waveform audio comments and branch audio mixes.");
        p1.setTechnologies("React, TypeScript, Web Audio API, Spring Boot");
        p1.setCategory("Web");
        p1.setStatus("IN_PROGRESS");
        p1.setGithubUrl("https://github.com/rohandeshmukh/echoflow");
        p1.setDemoUrl("https://echoflow.nexus.app");
        p1.setCreator(rohan);
        p1.setRescue(false);
        p1 = projectRepository.save(p1);
        projectMemberRepository.save(new ProjectMember(p1, rohan, "Lead Frontend Engineer"));
        projectMemberRepository.save(new ProjectMember(p1, aarav, "Backend Architect"));

        ProjectUpdate u1 = new ProjectUpdate();
        u1.setProject(p1);
        u1.setTitle("V1 Audio Engine Shipped");
        u1.setDescription("Integrated multi-track Web Audio API decoding with synchronized playheads.");
        u1.setMilestones("Multi-track decoding, waveform rendering, marker annotations");
        u1.setChallenges("Audio buffer drift across high-sample-rate tracks");
        u1.setLessonsLearned("Web Workers are essential for non-blocking waveform spectrum calculations.");
        projectUpdateRepository.save(u1);

        // Project 2: Ghost Project Rescue!
        Project p2 = new Project();
        p2.setTitle("AutoGrader CLI & Sandbox");
        p2.setShortDescription("Lightweight offline assignment test runner for computer science teaching assistants.");
        p2.setDetailedDescription("Started during sophomore year to automate grading student Java submissions. Stalled when Docker sandbox security and student test isolation got complicated.");
        p2.setTechnologies("Java, Docker, Linux cgroups");
        p2.setCategory("Open Source");
        p2.setStatus("NEEDS_RESCUE");
        p2.setRescue(true);
        p2.setRescueBlockers("Stuck on Docker container isolation & safe timeout enforcement under Windows and Linux environments without root privileges.");
        p2.setRescueSkillsNeeded("Docker daemon security, Linux cgroups, CLI ergonomics");
        p2.setGithubUrl("https://github.com/aaravsharma/autograder-cli");
        p2.setCreator(aarav);
        p2 = projectRepository.save(p2);
        projectMemberRepository.save(new ProjectMember(p2, aarav, "Author"));

        // Build Circle 1
        BuildCircle c1 = new BuildCircle();
        c1.setName("Algorithmic Trading Bot Collective");
        c1.setDescription("Building an open backtester for crypto and equity paper trading using order book event simulation.");
        c1.setTechnologies("Python, Pandas, FastAPI, React");
        c1.setMaxMembers(4);
        c1.setOpenPositions("Frontend Data Vis Engineer, Low-Latency Python Dev");
        c1.setProjectGoal("Backtest and benchmark 3 momentum strategies across 2 years of simulated tick data by semester end.");
        c1.setProgressStatus("IN_PROGRESS");
        c1.setCreator(ananya);
        c1 = circleRepository.save(c1);
        circleMemberRepository.save(new CircleMember(c1, ananya, "CREATOR"));
        circleMemberRepository.save(new CircleMember(c1, priya, "MEMBER"));

        CircleMilestone m1 = new CircleMilestone(c1, "Market Data Pipeline Setup", "Ingest binance and historical ticks", LocalDate.now().minusDays(10));
        m1.setCompleted(true);
        m1.setCompletedAt(LocalDateTime.now().minusDays(9));
        circleMilestoneRepository.save(m1);

        CircleMilestone m2 = new CircleMilestone(c1, "Backtesting Engine Core", "Implement order execution matching and slippage models", LocalDate.now().plusDays(15));
        circleMilestoneRepository.save(m2);

        CircleMilestone m3 = new CircleMilestone(c1, "Web Performance Dashboard", "Build charts for Sharpe ratio, drawdowns, and portfolio equity", LocalDate.now().plusDays(30));
        circleMilestoneRepository.save(m3);

        // Skill Bounty 1
        SkillBounty b1 = new SkillBounty();
        b1.setTitle("Help me configure Spring Security CSRF for Fetch API");
        b1.setDescription("Getting 403 Forbidden on my POST requests with custom credentials. Need an experienced Spring Boot dev to pair with me for 30 minutes.");
        b1.setSkillName("Spring Boot");
        b1.setBountyType("DEBUGGING");
        b1.setStatus("OPEN");
        b1.setCreator(rohan);
        bountyRepository.save(b1);

        // Skill Bounty 2
        SkillBounty b2 = new SkillBounty();
        b2.setTitle("Code review our PyTorch DataLoader memory leak");
        b2.setDescription("Training worker processes are ballooning memory on large image datasets. Looking for a senior ML engineer to review dataset caching strategy.");
        b2.setSkillName("PyTorch");
        b2.setBountyType("CODE_REVIEW");
        b2.setStatus("OPEN");
        b2.setCreator(ananya);
        bountyRepository.save(b2);
    }

    private void addSkill(User user, String name, String category, String type, String level) {
        Skill skill = skillRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> skillRepository.save(new Skill(name, category)));
        UserSkill us = new UserSkill(user, skill, type, level);
        userSkillRepository.save(us);
    }
}
