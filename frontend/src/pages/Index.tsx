import { Hero } from "@/components/ui/animated-hero";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { LayoutDashboard, LogOut, CheckCircle, Clock, Zap, Code, Database, Brain } from "lucide-react";
import { GlowingEffect } from "@/components/ui/glowing-effect";
import { useNavigate } from "react-router-dom";

const Index = () => {
  const { username, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-background relative">
      {/* Header / Navbar Overlay */}
      <div className="absolute top-4 right-4 z-50">
        {username ? (
          <div className="flex items-center gap-4">
            <span className="text-sm font-medium text-muted-foreground hidden sm:inline-block">
              Welcome, <span className="font-bold text-foreground">{username}</span>
            </span>
            <Button variant="ghost" size="sm" onClick={() => navigate("/dashboard")}>
              <LayoutDashboard className="w-4 h-4 mr-2" />
              Dashboard
            </Button>
            <Button variant="outline" size="sm" onClick={logout}>
              <LogOut className="w-4 h-4 mr-2" />
              Logout
            </Button>
          </div>
        ) : (
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="sm" onClick={() => navigate("/login")}>
              Login
            </Button>
            <Button variant="default" size="sm" onClick={() => navigate("/register")}>
              Register
            </Button>
          </div>
        )}
      </div>

      <Hero />

      {/* Why Choose Us Section */}
      <section className="py-20 relative z-10">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-5xl font-bold mb-4 bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
              Why Smart Product Analyzer?
            </h2>
            <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
              We combine advanced AI with real-time data to give you the most accurate product insights available.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-6">
            <div className="relative h-full rounded-[1.25rem] border-[0.75px] border-border p-2 md:rounded-[1.5rem] md:p-3">
              <GlowingEffect
                spread={40}
                glow={true}
                disabled={false}
                proximity={64}
                inactiveZone={0.01}
                borderWidth={3}
              />
              <div className="relative flex h-full flex-col justify-between gap-6 overflow-hidden rounded-xl border-[0.75px] bg-background p-6 shadow-sm dark:shadow-[0px_0px_27px_0px_rgba(45,45,45,0.3)]">
                <div className="relative flex flex-1 flex-col justify-between gap-3">
                  <div className="w-fit rounded-lg border-[0.75px] border-border bg-muted p-2">
                    <Zap className="h-6 w-6 text-primary" />
                  </div>
                  <div className="space-y-3">
                    <h3 className="pt-0.5 text-xl font-semibold font-sans tracking-tight text-foreground">
                      AI-Powered Precision
                    </h3>
                    <p className="font-sans text-sm/[1.375rem] text-muted-foreground">
                      Our advanced algorithms analyze thousands of reviews to extract hidden pros and cons that standard descriptions miss.
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div className="relative h-full rounded-[1.25rem] border-[0.75px] border-border p-2 md:rounded-[1.5rem] md:p-3">
              <GlowingEffect
                spread={40}
                glow={true}
                disabled={false}
                proximity={64}
                inactiveZone={0.01}
                borderWidth={3}
              />
              <div className="relative flex h-full flex-col justify-between gap-6 overflow-hidden rounded-xl border-[0.75px] bg-background p-6 shadow-sm dark:shadow-[0px_0px_27px_0px_rgba(45,45,45,0.3)]">
                <div className="relative flex flex-1 flex-col justify-between gap-3">
                  <div className="w-fit rounded-lg border-[0.75px] border-border bg-muted p-2">
                    <Clock className="h-6 w-6 text-primary" />
                  </div>
                  <div className="space-y-3">
                    <h3 className="pt-0.5 text-xl font-semibold font-sans tracking-tight text-foreground">
                      Save Hours of Research
                    </h3>
                    <p className="font-sans text-sm/[1.375rem] text-muted-foreground">
                      Stop opening dozens of tabs. Get a comprehensive summary and "best buy" recommendation in seconds.
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div className="relative h-full rounded-[1.25rem] border-[0.75px] border-border p-2 md:rounded-[1.5rem] md:p-3">
              <GlowingEffect
                spread={40}
                glow={true}
                disabled={false}
                proximity={64}
                inactiveZone={0.01}
                borderWidth={3}
              />
              <div className="relative flex h-full flex-col justify-between gap-6 overflow-hidden rounded-xl border-[0.75px] bg-background p-6 shadow-sm dark:shadow-[0px_0px_27px_0px_rgba(45,45,45,0.3)]">
                <div className="relative flex flex-1 flex-col justify-between gap-3">
                  <div className="w-fit rounded-lg border-[0.75px] border-border bg-muted p-2">
                    <CheckCircle className="h-6 w-6 text-primary" />
                  </div>
                  <div className="space-y-3">
                    <h3 className="pt-0.5 text-xl font-semibold font-sans tracking-tight text-foreground">
                      Unbiased & Objective
                    </h3>
                    <p className="font-sans text-sm/[1.375rem] text-muted-foreground">
                      We don't sell products. Our only goal is to help you find the absolute best option for your needs and budget.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Tech Stack Section */}
      <section className="py-20 relative z-10 bg-black/20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-5xl font-bold mb-4 bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent">
              Built with Modern Tech
            </h2>
            <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
              Leveraging the power of the latest frameworks and AI models for speed and intelligence.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-6">
            <div className="relative h-full rounded-[1.25rem] border-[0.75px] border-border p-2 md:rounded-[1.5rem] md:p-3">
              <GlowingEffect
                spread={40}
                glow={true}
                disabled={false}
                proximity={64}
                inactiveZone={0.01}
                borderWidth={3}
              />
              <div className="relative flex h-full flex-col justify-between gap-6 overflow-hidden rounded-xl border-[0.75px] bg-background p-6 shadow-sm dark:shadow-[0px_0px_27px_0px_rgba(45,45,45,0.3)]">
                <div className="relative flex flex-1 flex-col justify-between gap-3">
                  <div className="w-fit rounded-lg border-[0.75px] border-border bg-muted p-2">
                    <Code className="h-6 w-6 text-accent" />
                  </div>
                  <div className="space-y-3">
                    <h3 className="pt-0.5 text-xl font-semibold font-sans tracking-tight text-foreground">
                      Frontend Excellence
                    </h3>
                    <p className="font-sans text-sm/[1.375rem] text-muted-foreground">
                      Built with React, TypeScript, and Tailwind CSS. Featuring Shadcn UI and Framer Motion for a fluid, premium experience.
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div className="relative h-full rounded-[1.25rem] border-[0.75px] border-border p-2 md:rounded-[1.5rem] md:p-3">
              <GlowingEffect
                spread={40}
                glow={true}
                disabled={false}
                proximity={64}
                inactiveZone={0.01}
                borderWidth={3}
              />
              <div className="relative flex h-full flex-col justify-between gap-6 overflow-hidden rounded-xl border-[0.75px] bg-background p-6 shadow-sm dark:shadow-[0px_0px_27px_0px_rgba(45,45,45,0.3)]">
                <div className="relative flex flex-1 flex-col justify-between gap-3">
                  <div className="w-fit rounded-lg border-[0.75px] border-border bg-muted p-2">
                    <Database className="h-6 w-6 text-accent" />
                  </div>
                  <div className="space-y-3">
                    <h3 className="pt-0.5 text-xl font-semibold font-sans tracking-tight text-foreground">
                      Robust Backend
                    </h3>
                    <p className="font-sans text-sm/[1.375rem] text-muted-foreground">
                      Powered by Java Spring Boot for stability and performance. Uses Selenium for real-time web scraping without limits.
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div className="relative h-full rounded-[1.25rem] border-[0.75px] border-border p-2 md:rounded-[1.5rem] md:p-3">
              <GlowingEffect
                spread={40}
                glow={true}
                disabled={false}
                proximity={64}
                inactiveZone={0.01}
                borderWidth={3}
              />
              <div className="relative flex h-full flex-col justify-between gap-6 overflow-hidden rounded-xl border-[0.75px] bg-background p-6 shadow-sm dark:shadow-[0px_0px_27px_0px_rgba(45,45,45,0.3)]">
                <div className="relative flex flex-1 flex-col justify-between gap-3">
                  <div className="w-fit rounded-lg border-[0.75px] border-border bg-muted p-2">
                    <Brain className="h-6 w-6 text-accent" />
                  </div>
                  <div className="space-y-3">
                    <h3 className="pt-0.5 text-xl font-semibold font-sans tracking-tight text-foreground">
                      Next-Gen AI
                    </h3>
                    <p className="font-sans text-sm/[1.375rem] text-muted-foreground">
                      Integrated various LLMs (GPT-OSS/Llama 3) via Fireworks AI to interpret complex data and deliver human-like insights.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Index;
