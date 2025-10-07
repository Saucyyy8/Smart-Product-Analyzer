import { useState } from "react";
import { Search, TrendingUp, ShieldCheck, Zap } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import HeroSection from "@/components/HeroSection";
import SearchInterface from "@/components/SearchInterface";
import ResultsDisplay from "@/components/ResultsDisplay";
import { useAuth } from "@/contexts/AuthContext";

const Index = () => {
  const [searchResults, setSearchResults] = useState(null);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const { toast } = useToast();
  const { token, logout, username } = useAuth();

  const handleAnalysis = async (input: string, type: 'url' | 'description') => {
    setIsAnalyzing(true);

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    try {
      // Call your Spring Boot backend API
      const response = await fetch('http://localhost:8080/product/analyze', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({
          input: input,
          type: type
        })
      });

      if (!response.ok) {
        if (response.status === 401) {
          logout();
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const product = await response.json();
      
      // Transform single product response to match UI expectations
      const results = {
        query: input,
        products: [
          {
            id: 1,
            name: product.name,
            price: product.price,
            rating: product.rating || (product.rating !== undefined ? product.rating / 10 * 5 : 4.5), // Convert 0-10 scale to 0-5 if needed
            pros: product.pros || [],
            cons: product.cons || [],
            score: product.rating || 85, // Use rating as score
            isRecommended: (product.rating || 7) > 7, // Recommend if rating > 7
            verdict: product.verdict,
            url: product.url
          }
        ]
      };
      
      setSearchResults(results);
      toast({
        title: "Analysis Complete!",
        description: "Found and analyzed the best products for you.",
      });
    } catch (error) {
      console.error('Analysis failed:', error);
      if (!(error instanceof Error && error.message.includes('401'))) {
        toast({
          title: "Analysis Failed",
          description: error instanceof Error 
            ? `Error: ${error.message}. Make sure your Spring Boot backend is running on localhost:8080.`
            : "Please try again or check your connection.",
          variant: "destructive",
        });
      }
    } finally {
      setIsAnalyzing(false);
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <HeroSection username={username} logout={logout} />
      <SearchInterface onAnalyze={handleAnalysis} isAnalyzing={isAnalyzing} />
      {searchResults && (
        <ResultsDisplay results={searchResults} />
      )}
    </div>
  );
};

export default Index;
