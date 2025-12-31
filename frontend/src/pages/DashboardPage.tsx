import { useEffect, useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { Card, CardContent } from "@/components/ui/card";
import { GlowingEffect } from "@/components/ui/glowing-effect";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, Star, ExternalLink, Calendar } from "lucide-react";
import { format } from "date-fns";

interface ProductHistory {
  id: number;
  searchQuery: string;
  productName: string;
  productRating: number;
  summary: string;
  verdict: string;
  imageUrl?: string;
  productUrl?: string;
  createdAt: string;
}

const DashboardPage = () => {
  const { token, logout } = useAuth();
  const [history, setHistory] = useState<ProductHistory[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const response = await fetch("http://localhost:8080/history", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (response.ok) {
          const data = await response.json();
          setHistory(data);
        } else if (response.status === 401) {
          logout();
        }
      } catch (error) {
        console.error("Failed to fetch history:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchHistory();
  }, [token, logout]);

  return (
    <div className="min-h-screen bg-background p-6">
      <div className="max-w-7xl mx-auto space-y-8">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" onClick={() => navigate("/")}>
              <ArrowLeft className="h-5 w-5" />
            </Button>
            <h1 className="text-3xl font-bold tracking-tight">Your Analysis History</h1>
          </div>
          <Button variant="outline" onClick={() => navigate("/")}>
            New Search
          </Button>
        </div>

        {loading ? (
          <div className="flex justify-center p-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
          </div>
        ) : history.length === 0 ? (
          <Card className="glass-card">
            <CardContent className="flex flex-col items-center justify-center p-12 space-y-4">
              <div className="h-12 w-12 rounded-full bg-muted flex items-center justify-center">
                <Calendar className="h-6 w-6 text-muted-foreground" />
              </div>
              <h3 className="text-xl font-semibold">No history yet</h3>
              <p className="text-muted-foreground text-center max-w-sm">
                Start analyzing products to build your history dashboard.
              </p>
              <Button onClick={() => navigate("/")} className="mt-4">
                Analyze First Product
              </Button>
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {history.map((item) => (
              <div key={item.id} className="relative h-full rounded-[1.25rem] border-[0.75px] border-border p-2 md:rounded-[1.5rem] md:p-3">
                <GlowingEffect
                  spread={40}
                  glow={true}
                  disabled={false}
                  proximity={64}
                  inactiveZone={0.01}
                  borderWidth={3}
                />
                <div className="relative flex h-full flex-col justify-between gap-6 overflow-hidden rounded-xl border-[0.75px] bg-background p-0 shadow-sm dark:shadow-[0px_0px_27px_0px_rgba(45,45,45,0.3)]">
                  <div className="aspect-video w-full bg-muted relative overflow-hidden group">
                    {item.imageUrl ? (
                      <img
                        src={item.imageUrl}
                        alt={item.productName}
                        className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center bg-secondary/20">
                        <span className="text-muted-foreground">No Image</span>
                      </div>
                    )}
                    <div className="absolute top-2 right-2">
                      <Badge variant={item.productRating >= 7 ? "default" : "secondary"} className="glass-card backdrop-blur-md">
                        <Star className="w-3 h-3 mr-1 fill-current" />
                        {item.productRating}/10
                      </Badge>
                    </div>
                  </div>

                  <div className="p-6 pt-2 space-y-4 flex-grow flex flex-col">
                    <div className="space-y-2">
                      <h3 className="text-lg font-semibold line-clamp-2 leading-tight">
                        {item.productName}
                      </h3>
                      <p className="text-xs text-muted-foreground flex items-center gap-1">
                        <Calendar className="w-3 h-3" />
                        {new Date(item.createdAt).toLocaleDateString()}
                      </p>
                    </div>

                    <div>
                      <p className="text-sm font-medium text-muted-foreground mb-1">Based on search:</p>
                      <p className="text-sm italic truncate">"{item.searchQuery}"</p>
                    </div>

                    {item.summary && (
                      <div className="p-3 bg-secondary/10 rounded-lg flex-grow">
                        <p className="text-sm line-clamp-3 text-muted-foreground">
                          {item.summary}
                        </p>
                      </div>
                    )}

                    {item.productUrl && (
                      <Button variant="outline" className="w-full gap-2 group-hover:border-primary/50 mt-auto" asChild>
                        <a href={item.productUrl} target="_blank" rel="noopener noreferrer">
                          View on Amazon <ExternalLink className="w-3 h-3 ml-1" />
                        </a>
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default DashboardPage;
