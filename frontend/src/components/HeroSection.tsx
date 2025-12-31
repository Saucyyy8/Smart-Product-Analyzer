import { TrendingUp, ShieldCheck, Zap, Search, LogOut, LayoutDashboard } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";

interface HeroSectionProps {
  username: string | null;
  logout: () => void;
}

const HeroSection = ({ username, logout }: HeroSectionProps) => {
  const navigate = useNavigate();
  const scrollToSearch = () => {
    const searchSection = document.getElementById('search-section');
    searchSection?.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <section className="relative overflow-hidden bg-gradient-to-br from-background via-muted/20 to-accent/10">
      {/* Background Pattern */}
      <div className="absolute inset-0 bg-grid-pattern opacity-5" />

      {/* User Info and Logout */}
      {/* User Info and Logout */}
      <div className="absolute top-4 right-4 z-10">
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

      <div className="container mx-auto px-4 py-20 lg:py-32">
        <div className="text-center max-w-4xl mx-auto">
          {/* Badge */}
          <div className="inline-flex items-center gap-2 bg-primary/10 border border-primary/20 rounded-full px-4 py-2 mb-8">
            <Zap className="w-4 h-4 text-primary" />
            <span className="text-sm font-medium text-primary">AI-Powered Product Analysis</span>
          </div>

          {/* Main Heading */}
          <h1 className="text-4xl md:text-6xl lg:text-7xl font-bold mb-6 bg-gradient-to-r from-foreground to-foreground/80 bg-clip-text text-transparent">
            Smart Product
            <br />
            <span className="bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent">
              Analyzer
            </span>
          </h1>

          {/* Subtitle */}
          <p className="text-xl md:text-2xl text-muted-foreground mb-12 max-w-3xl mx-auto leading-relaxed">
            Enter a product URL or description, and get intelligent analysis with pros, cons,
            and personalized recommendations from top Amazon results.
          </p>

          {/* CTA Buttons */}
          <div className="flex flex-col sm:flex-row gap-4 justify-center mb-16">
            <Button
              variant="hero"
              size="lg"
              onClick={scrollToSearch}
              className="glow-primary"
            >
              <Search className="w-5 h-5 mr-2" />
              Start Analysis
            </Button>
            <Button variant="outline" size="lg">
              Watch Demo
            </Button>
          </div>

          {/* Feature Cards */}
          <div className="grid md:grid-cols-3 gap-6 max-w-4xl mx-auto">
            <div className="feature-card text-center group">
              <div className="w-12 h-12 bg-gradient-to-r from-primary to-primary/80 rounded-xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform duration-300">
                <Search className="w-6 h-6 text-primary-foreground" />
              </div>
              <h3 className="text-xl font-semibold mb-2">Smart Search</h3>
              <p className="text-muted-foreground">
                Automatically builds targeted searches and analyzes top Amazon results
              </p>
            </div>

            <div className="feature-card text-center group">
              <div className="w-12 h-12 bg-gradient-to-r from-accent to-accent/80 rounded-xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform duration-300">
                <TrendingUp className="w-6 h-6 text-accent-foreground" />
              </div>
              <h3 className="text-xl font-semibold mb-2">Deep Analysis</h3>
              <p className="text-muted-foreground">
                Identifies pros, cons, and key features to help you make informed decisions
              </p>
            </div>

            <div className="feature-card text-center group">
              <div className="w-12 h-12 bg-gradient-to-r from-success to-success/80 rounded-xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform duration-300">
                <ShieldCheck className="w-6 h-6 text-success-foreground" />
              </div>
              <h3 className="text-xl font-semibold mb-2">Best Recommendations</h3>
              <p className="text-muted-foreground">
                Get personalized product recommendations based on comprehensive analysis
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Gradient Orbs */}
      <div className="absolute top-20 left-10 w-32 h-32 bg-gradient-to-r from-primary/30 to-accent/30 rounded-full blur-3xl animate-pulse" />
      <div className="absolute bottom-20 right-10 w-40 h-40 bg-gradient-to-r from-accent/20 to-success/20 rounded-full blur-3xl animate-pulse delay-1000" />
    </section>
  );
};

export default HeroSection;
